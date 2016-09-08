package ru.agny.xent

import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class MessageQueue() {

  var messages: Seq[(Message, Long)] = Seq.empty
  val last: AtomicLong = new AtomicLong(-1)

  def push(msg: Message, number: Long): Future[Long] = {
    push_rec(msg, last.get(), number)
  }

  private def push_rec(msg: Message, init: Long, number: Long): Future[Long] = {
    if (!last.compareAndSet(init, number))
      push_rec(msg, last.get(), number)
    else {
      println(s"MESSAGE: $msg")
      messages = (msg, number) +: messages
      Future(number)
    }
  }

  def take(): Seq[Message] = this.synchronized {
    val res = messages.map(_._1).reverse
    messages = Seq.empty
    res
  }
}
