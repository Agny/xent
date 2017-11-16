package ru.agny.xent.trade

import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

/**
  * Lot with Bid-based price
  * It remains on the Board until time is up or buyout is met
  */
case class NonStrict(id: Long, user: UserId, item: Item, buyout: Price, until: TimeStamp, lastBid: Option[Bid]) extends Biddable {

}
