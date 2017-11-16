package ru.agny.xent.trade

import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

case class Dealer(id: Long, user: UserId, item: ItemStack, buyout: Price, until: TimeStamp) extends Lot {

}
