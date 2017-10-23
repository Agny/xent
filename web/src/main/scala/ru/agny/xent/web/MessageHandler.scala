package ru.agny.xent.web

import java.util.concurrent.atomic.AtomicLong

import io.netty.channel.Channel
import ru.agny.xent.messages.{MessageQueue, ReactiveLog, ResponseOk}
import ru.agny.xent.web.utils.ReactiveToSocket

case class MessageHandler(queue: MessageQueue[ReactiveLog]) {

  val last = new AtomicLong(0)

  def send(msg: ReactiveLog, channel: Channel): Unit = {
    queue.push(ReactiveToSocket(msg, channel), last.incrementAndGet())
  }

  def sendTest(msg: ReactiveLog) = {
    queue.push(msg, last.incrementAndGet())
    ResponseOk
  }
}
