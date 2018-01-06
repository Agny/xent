package ru.agny.xent.trade

import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId

/**
  * Lot with strictly defined price
  */
case class Strict(id: Long, user: UserId, item: ItemHolder, buyout: ItemHolder, until: TimeStamp) extends Lot

object Strict {
  val `type`: LotType = Strict(0, 0, ItemHolder(0, 0), ItemHolder(0, 0), 0).tpe
}
