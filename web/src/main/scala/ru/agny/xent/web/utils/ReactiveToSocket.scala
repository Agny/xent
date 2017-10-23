package ru.agny.xent.web.utils

import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import ru.agny.xent.messages.{PlainResponse, ReactiveLog}

import scala.util.{Failure, Success}

case class ReactiveToSocket(r: ReactiveLog, socket: Channel) extends ReactiveLog {

  import scala.concurrent.ExecutionContext.Implicits.global

  override val user = r.user
  override val layer = r.layer

  override def respond(value: PlainResponse) = {
    r.respond(value).andThen {
      case s@Success(v) =>
        socket.writeAndFlush(new TextWebSocketFrame(v.value)) //TODO actual response
        s
      case f@Failure(v) => f //TODO make client aware of this error
    }
  }

  override def collectionId = r.collectionId

  override def key = r.key

  override def toPersist = r.toPersist
}
