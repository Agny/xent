package ru.agny.xent.trade

import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

case class Dealer(id: Long, user: UserId, item: ItemStack, buyout: ItemStack, until: TimeStamp) extends Lot

object Dealer {
  val `type`: LotType = Dealer(0, 0, ItemStack(0, 0, 0), ItemStack(0, 0, 0), 0).tpe
}
