package ru.agny.xent.action

import ru.agny.xent.core.utils.ItemIdGenerator
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.core.Layer
import ru.agny.xent.messages.{ReactiveLog, ResponseOk}

case class CreateTroop(id: UserId, souls: Vector[Long], src: ReactiveLog) extends LayerAction {
  override def run(layer: Layer) = {
    val res = for {
      user <- layer.getUser(id)
      userWithTroop <- user.createTroop(ItemIdGenerator.next, souls)
    } yield {
      val (updateUser, troop) = userWithTroop
      val updatedLayer = layer.copy(users = updateUser +: layer.users.filterNot(_.id == user.id))
      updatedLayer.addTroop(troop)
    }
    res match {
      case Left(v) => src.failed(v); layer
      case Right(v) => src.respond(ResponseOk); v
    }
  }
}
