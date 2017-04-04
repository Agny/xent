package ru.agny.xent.core

import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core.Progress.ProductionTime

sealed trait Item {
  val id: ItemId
}
trait SingleItem extends Item
trait DelayableItem extends Item {
  val yieldTime: ProductionTime
}
sealed trait StackableItem extends Item {
  val stackValue: Int
}

final case class ResourceUnit(stackValue: Int, id: ItemId) extends StackableItem

object Progress {
  type ProductionTime = Long
}
object Item {
  type ItemId = Long
}
