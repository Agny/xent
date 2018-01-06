package ru.agny.xent.trade

import ru.agny.xent.core.utils.TimeUnit.TimeStamp
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.trade.Lot.LotId

trait Lot {
  val id: LotId
  val user: UserId
  val item: ItemHolder
  val buyout: ItemHolder
  val until: TimeStamp
  val created: TimeStamp = System.currentTimeMillis()
  val tpe: LotType = LotType(getClass.getSimpleName)
}
object Lot {
  type LotId = Long
}
