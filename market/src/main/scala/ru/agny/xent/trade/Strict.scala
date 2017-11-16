package ru.agny.xent.trade

import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

/**
  * Lot with strictly defined price
  */
case class Strict(id: Long, user: UserId, item: Item, buyout: Price, until: TimeStamp) extends Lot {

}
