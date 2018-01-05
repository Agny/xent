package ru.agny.xent.trade

import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

/**
  * Lot with Bid-based price
  * It remains on the Board until time is up or buyout is met
  */
case class NonStrict(id: Long, user: UserId, item: ItemStack, buyout: ItemStack, until: TimeStamp, lastBid: Option[Bid]) extends Lot

object NonStrict {
  val `type`: LotType = NonStrict(0, 0, ItemStack(0, 0, 0), ItemStack(0, 0, 0), 0, None).tpe
}
