package ru.agny.xent

import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class MessageQueue() {

  var messages: List[(Message, Long)] = List.empty
  val last: AtomicLong = new AtomicLong(-1)

  def push(msg: Message, number: Long): Future[Long] = {
    push_rec(msg, last.get(), number)
  }

  private def push_rec(msg: Message, init: Long, number: Long): Future[Long] = {
    if (!last.compareAndSet(init, number))
      push_rec(msg, last.get(), number)
    else {
      println(msg)
      messages = (msg, number) :: messages
      Future(number)
    }
  }

  def take(): List[Message] = this.synchronized {
    val res = messages.map(_._1).reverse
    messages = List.empty
    println(s"Messages to handle ${res.size}")
    res
  }
}
