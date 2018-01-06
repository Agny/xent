package ru.agny.xent.trade

import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

case class PlaceLot(user: UserId, item: ItemHolder, buyout: ItemHolder, minPrice: Option[ItemHolder], until: TimeStamp, tpe: LotType)
