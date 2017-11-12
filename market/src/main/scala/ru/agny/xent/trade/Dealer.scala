package ru.agny.xent.trade

import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

case class Dealer(user: UserId, item: Item, buyout: Price, until: TimeStamp) extends Lot {

}
