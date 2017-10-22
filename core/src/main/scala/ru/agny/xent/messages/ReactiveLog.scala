package ru.agny.xent.messages

import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.{RedisAdapter, RedisEntity, Loggable}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ReactiveLog extends Message with Loggable with Responder[PlainResponse] {

  def respond(value: PlainResponse): Future[PlainResponse] = Future {
    println(s"REPLY: $this-$value")
    RedisAdapter.set(this)
    value
  }
}
@RedisEntity("user", "user", System.nanoTime().toString)
case class EmptyMessage(user: UserId, layer: String) extends ReactiveLog
@RedisEntity("user", "user", System.nanoTime().toString)
case class NewUserMessage(user: UserId, name: String, layer: String) extends ReactiveLog
@RedisEntity("user", "user", System.nanoTime().toString)
case class LayerUpMessage(user: UserId, layer: String, layerTo: String) extends ReactiveLog