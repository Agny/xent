package ru.agny.xent.action

import ru.agny.xent.core.User
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.messages.{ReactiveLog, ResponseOk}

case class AddProduction(facility: ItemId, res: ItemStack, src: ReactiveLog) extends UserAction {
  override def run(user: User) = {
    user.addProduction(facility, res) match {
      case Left(v) => src.failed(v); user
      case Right(v) => src.respond(ResponseOk); v
    }
  }
}
