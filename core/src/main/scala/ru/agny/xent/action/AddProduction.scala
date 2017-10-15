package ru.agny.xent.action

import ru.agny.xent.core.User
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.ItemStack

case class AddProduction(facility: ItemId, res: ItemStack) extends UserAction {
  override def run(user: User) = user.addProduction(facility, res)
}
