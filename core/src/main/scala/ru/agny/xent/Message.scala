package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.Cell

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Message {
  val user: UserId

  def reply(value: Response): Future[Response] = Future {
    println(s"REPLY: $this-$value")
    value
  }
}
case class EmptyMessage(user: UserId, layer: String) extends Message
case class NewUserMessage(user: UserId, name: String, layer: String) extends Message
case class LayerUpMessage(user: UserId, layerFrom: String, layerTo: String) extends Message
case class ResourceClaimMessage(user: UserId, layer: String, facility: String, cell: Cell) extends Message
case class BuildingConstructionMessage(user: UserId, layer: String, building: String, cell: Cell) extends Message
case class AddProductionMessage(user: UserId, layer: String, facility: String, res: ResourceUnit) extends Message