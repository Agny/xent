package ru.agny.xent.persistence.slick

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, ThreadFactory}

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

trait ConfigurableRepository extends LazyLogging {
  implicit val ioContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(
    4,
    new ThreadFactory {
      private val counter = new AtomicLong(0L)

      def newThread(r: Runnable) = {
        val th = new Thread(r)
        th.setName(s"jdbc-io-thread-${counter.getAndIncrement}")
        th.setDaemon(true)
        th
      }
    }))
  val configPath: String
  val db = CoreInitializer.forConfig(configPath).db
}
