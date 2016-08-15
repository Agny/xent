package ru.agny.xent

import java.util.concurrent.atomic.AtomicLong

import io.netty.channel.Channel

case class MessageHandler(queue: MessageQueue) {

  val last = new AtomicLong(0)

  def send(msg: Message, channel: Channel) = {
    queue.push(msg, last.incrementAndGet())
    ResponseOk
  }

  def sendTest(msg: Message) = {
    queue.push(msg, last.incrementAndGet())
    ResponseOk
  }
}
