package ru.agny.xent

import com.typesafe.config.ConfigFactory
import ru.agny.xent.Message.{Notification, _}
import ru.agny.xent.Action._
import io.circe.syntax._

object MainLoop {

  def main(args:Array[String]):Unit = {
    val c = ConfigFactory.load()
    println("loaded")
    val ep = MessagePool[Event,Notification](c)
    while (true) {
      ep.take()
      Thread.sleep(2000)
    }
//    println(Event(1,100,Noop).asJson)
    /*
    for {
      events = pool.take()
      (nState, notifications) <- state.apply(events)
      _ <- pool.submit(notifications)
      _ <- storage.persist(nState)
    } yield nState
     */
  }
}
