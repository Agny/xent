package ru.agny.xent

import java.nio.charset.StandardCharsets
import java.util.Properties
import java.util.concurrent.TimeUnit

import scala.jdk.CollectionConverters._
import scala.jdk.DurationConverters._
import com.typesafe.config.Config
import io.circe.{Decoder, Encoder, Json, Printer}
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{Serde, Serdes}
import ru.agny.xent.Message._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.reflect.{ClassTag, Manifest, classTag}
import scala.util.{Failure, Success, Try}

trait MessagePool[In <: Message, Out <: Message] {
  def take(): Seq[In]

  def submit(responses: Seq[Out]): Unit
}
object MessagePool extends LazyLoggingDotty {
  def apply[In <: Message, Out <: Message](conf: MessagePoolConf)(
      using Encoder[In],
      Encoder[Out],
      Decoder[In],
      Decoder[Out]
  ): MessagePool[In, Out] = new DefaultImpl[In, Out](conf)

  def apply[In <: Message, Out <: Message](conf: Config)(
      using Encoder[In],
      Encoder[Out],
      Decoder[In],
      Decoder[Out]
  ): MessagePool[In, Out] = {
    val kafkaConf = conf.getConfig("kafka")
    val ec = MessagePoolConf(
      appId = kafkaConf.getString("appId"),
      bootstrap = kafkaConf.getString("bootstrap.servers"),
      inputTopic = kafkaConf.getString("topic.in"),
      outputTopic = kafkaConf.getString("topic.out")
    )
    MessagePool(ec)
  }

  private class DefaultImpl[In <: Message, Out <: Message](conf: MessagePoolConf)(
      using Encoder[In],
      Encoder[Out],
      Decoder[In],
      Decoder[Out]
  ) extends MessagePool[In, Out] {

    val props = new Properties()
    props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, conf.bootstrap)
    props.put(CommonClientConfigs.GROUP_ID_CONFIG, conf.appId)
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, conf.isAutoCommit)
    props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, conf.maxPollRecords)
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, conf.commitInterval)
    props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, conf.maxRequestSize)

    val producer = new KafkaProducer[Unit, Out](
      props,
      KafkaSerde.unitSerializer,
      KafkaSerde.serializer[Out]()
    )
    private var buffer: Buffer = new Buffer()

    new Thread(new Runnable {
      override def run(): Unit = {
        buffer = new Buffer()
        buffer.startPollingLoop()
      }
    }).start()

    override def take() = buffer.takeAndClear()

    override def submit(responses: Seq[Out]) = {
      logger.debug(s"Sending ${responses}")
      responses.map { r =>
        val record = new ProducerRecord[Unit, Out](conf.outputTopic, r)
        producer.send(record).get()
      }
    }
    
    private class Buffer() {
      private val state = ArrayBuffer.empty[In]
      private var isAlreadyStarted = false

      def add(a: In): Unit = this.synchronized {
        state += a
      }

      def takeAndClear(): Seq[In] = this.synchronized {
        logger.debug(s"Taking from buffer ${state.size} messages")
        val r = state.toSeq
        state.clear()
        r
      }

      private val consumer = {
        new KafkaConsumer[Unit, In](
          props,
          KafkaSerde.unitDeserializer,
          KafkaSerde.deserializer[In]()
        )
      }

      def startPollingLoop(): Unit = {
        while (!isAlreadyStarted) {
          consumer.subscribe(Seq(conf.inputTopic).asJavaCollection)
          val polled = consumer.poll(ScalaDurationOps(FiniteDuration(conf.maxPollDuration, TimeUnit.MILLISECONDS)).toJava)
          polled.partitions().forEach { p =>
            polled.records(p).forEach { r =>
              logger.debug(s"Polled ${r}")
              add(r.value)
            }
          }
          Thread.sleep(100)
        }
        isAlreadyStarted = true
      }
    }
  }
}
