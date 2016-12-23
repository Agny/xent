package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.Coordinate
import ru.agny.xent.persistence.{RedisEntity, RedisAdapter, RedisMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Message extends RedisMessage {
  val user: UserId
  val layer: String

  def reply(value: Response): Future[Response] = Future {
    println(s"REPLY: $this-$value")
    RedisAdapter.set(this)
    value
  }
}
@RedisEntity("user", "user", System.nanoTime().toString)
case class EmptyMessage(user: UserId, layer: String) extends Message
@RedisEntity("user", "user", System.nanoTime().toString)
case class NewUserMessage(user: UserId, name: String, layer: String) extends Message
@RedisEntity("user", "user", System.nanoTime().toString)
case class LayerUpMessage(user: UserId, layer: String, layerTo: String) extends Message
@RedisEntity("user", "user", System.nanoTime().toString)
case class ResourceClaimMessage(user: UserId, layer: String, facility: String, cell: Coordinate) extends Message
@RedisEntity("user", "user", System.nanoTime().toString)
case class BuildingConstructionMessage(user: UserId, layer: String, building: String, cell: Coordinate) extends Message
@RedisEntity("user", "user", System.nanoTime().toString)
case class AddProductionMessage(user: UserId, layer: String, facility: String, res: ResourceUnit) extends Message