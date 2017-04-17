package ru.agny.xent.core

import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core.Progress.ProductionTime
import ru.agny.xent.core.inventory.{EmptySlot, ItemSlot, Slot}

sealed trait Item {
  val id: ItemId
}
trait SingleItem extends Item
trait DelayableItem extends Item {
  val yieldTime: ProductionTime
}
sealed trait StackableItem extends Item {
  val stackValue: Int

  def add(v: StackableItem): StackableItem
}

final case class ResourceUnit(stackValue: Int, id: ItemId) extends StackableItem {
  override def add(v: StackableItem): ResourceUnit =
    if (v.id == id) ResourceUnit(v.stackValue + stackValue, id)
    else this
}

object Progress {
  type ProductionTime = Long
}
object Item {
  type ItemId = Long

  object implicits {
    implicit def convert(v: StackableItem): Slot[Item] = v match {
      case ResourceUnit(stackValue, id) if stackValue > 0 => ItemSlot(v)
      case _ => EmptySlot
    }

    implicit def convert(v: Vector[StackableItem])(implicit toSlot: StackableItem => Slot[Item]): Vector[Slot[Item]] = v.map(toSlot)
  }
}