package ru.agny.xent

import ru.agny.xent.UserType.UserId

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Message {
  val user: UserId

  def reply(value: Response): Future[Response] = Future {
    println(value)
    value
  }
}
case class EmptyMessage(user: UserId) extends Message
case class NewUserMessage(user: UserId, name:String, layer: String) extends Message
case class LayerUpMessage(user: UserId, layerFrom:String, layerTo: String) extends Message
case class ResourceClaimMessage(user: UserId, layer: String, facility: String, resourceId: Long) extends Message
