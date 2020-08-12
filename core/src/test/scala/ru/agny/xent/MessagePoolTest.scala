package ru.agny.xent

import com.dimafeng.testcontainers.{ForAllTestContainer, KafkaContainer}
import org.scalactic.source.Position
import org.scalatest.{Args, Status}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

import scala.language.implicitConversions

import ru.agny.xent.Message._
import ru.agny.xent.Action._

class MessagePoolTest extends AnyFlatSpec with ForAllTestContainer {

  override val container = KafkaContainer()

  //Error:(12, 15) value should is not a member of String
  //  "DefaultMessagePool" should "x" in {
  given Position = Position("MessagePoolTest", "ru.agny.xent", 42)

  "DefaultMessagePool" should "submit and poll messages" in {
    val c = container.asInstanceOf[KafkaContainer]
    val conf = MessagePoolConf(
      appId = "test",
      bootstrap = c.bootstrapServers,
      inputTopic = "in",
      outputTopic = "out",
      maxPollDuration = 5000
    )
    val producerPoolConf = MessagePoolConf(
      appId = "producer",
      bootstrap = c.bootstrapServers,
      inputTopic = "out",
      outputTopic = "in"
    )
    val eventToNotificationPool = MessagePool[Event, Notification](conf)
    val notificationToEventPool = MessagePool[Notification, Event](producerPoolConf)

    val messages: Seq[Event] = Seq(Event(1, 1020, Noop).asInstanceOf[Event], Event(2, 1040, Noop).asInstanceOf[Event])
    notificationToEventPool.submit(messages)

    Thread.sleep(1000) //warmup

    eventToNotificationPool.take() should contain theSameElementsAs(messages)
  }


  override def run(testName: Option[String], args: Args): Status = super.run(testName, args)

  protected override def runTest(testName: String, args: Args): Status = super.runTest(testName, args)

}
