package ru.agny.xent.trade

import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

trait Lot {
  val user: UserId
  val item: Item
  val buyout: Price
  val until: TimeStamp
}
