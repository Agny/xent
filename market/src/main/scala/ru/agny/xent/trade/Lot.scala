package ru.agny.xent.trade

import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

trait Lot {
  val id: Long
  val user: UserId
  val item: ItemStack
  val buyout: Price
  val until: TimeStamp
}
