package ru.agny.xent.web

import io.netty.channel.Channel
import ru.agny.xent.messages._
import ru.agny.xent.web.utils.ReactiveToSocket

case class MessageHandler(queue: MessageQueue[Message]) {

  def send(msg: ReactiveLog, channel: Channel): Unit = {
    queue.push(ReactiveToSocket(msg, channel))
  }

  def sendTest(msg: ReactiveLog) = {
    queue.push(msg)
    ResponseOk
  }
}
