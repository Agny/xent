package ru.agny.xent.web

import java.util.concurrent.atomic.AtomicLong

import io.netty.channel.Channel
import ru.agny.xent.messages.{ReactiveLog, MessageQueue, ResponseOk}

case class MessageHandler(queue: MessageQueue[ReactiveLog]) {

  val last = new AtomicLong(0)

  def send(msg: ReactiveLog, channel: Channel) = {
    queue.push(msg, last.incrementAndGet())
    ResponseOk
  }

  def sendTest(msg: ReactiveLog) = {
    queue.push(msg, last.incrementAndGet())
    ResponseOk
  }
}
