package ru.agny.xent.messages

import ru.agny.xent.action.{DoNothing, LayerChange, NewUser}
import ru.agny.xent.core.utils.ErrorCode
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.persistence.redis.{Loggable, RedisAdapter, RedisEntity}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ReactiveLog extends ActiveMessage with Loggable {
  override type ResponseType = PlainResponse

  override def respond(value: PlainResponse): Future[PlainResponse] = Future {
    println(s"REPLY: $this-$value")
    RedisAdapter.set(this)
    value
  }

  override def failed(code: ErrorCode.Value): Unit = Future {
    println(s"FAILED: $this-$code")
    //TODO lookup codes and construct message
  }
}
@RedisEntity("user", "user", System.nanoTime().toString)
case class EmptyMessage(user: UserId, layer: String) extends ReactiveLog {
  override val action = DoNothing
}
@RedisEntity("user", "user", System.nanoTime().toString)
case class NewUserMessage(user: UserId, name: String, layer: String) extends ReactiveLog {
  override val action: NewUser = NewUser(user, name, this)
}
@RedisEntity("user", "user", System.nanoTime().toString)
case class LayerUpMessage(user: UserId, layer: String, layerTo: String) extends ReactiveLog {
  override val action = LayerChange(user, this)
}