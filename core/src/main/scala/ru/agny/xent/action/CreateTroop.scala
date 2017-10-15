package ru.agny.xent.action

import ru.agny.xent.core.utils.ItemIdGenerator
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.core.{Layer, User}
import ru.agny.xent.messages.Response

case class CreateTroop(id: UserId, souls: Vector[Long]) extends LayerAction {
  override def run(layer: Layer) = {
    for {
      user <- layer.getUser(id)
      userWithTroop <- user.createTroop(ItemIdGenerator.next, souls)
    } yield {
      val (updateUser, troop) = userWithTroop
      val updatedLayer = layer.copy(users = updateUser +: layer.users.filterNot(_.id == user.id))
      updatedLayer.addTroop(troop)
    }
  }
}
