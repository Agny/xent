package ru.agny.xent.core.inventory

import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.Progress.ProgressTime

trait Item {
  val id: ItemId
}
trait DelayableItem extends Item {
  val name: String
  val yieldTime: ProgressTime
}

final case class ItemStack(stackValue: Int, id: ItemId) extends Item {
  def add(v: ItemStack): ItemStack =
    if (v.id == id) ItemStack(v.stackValue + stackValue, id)
    else this
}

object Progress {
  type ProgressTime = Long
}
object Item {
  type ItemId = Long

  object implicits {
    implicit def convert(v: ItemStack): Slot[Item] = v match {
      case ItemStack(stackValue, _) if stackValue > 0 => ItemSlot(v)
      case _ => EmptySlot
    }

    implicit def convert(v: Vector[ItemStack])(implicit toSlot: ItemStack => Slot[Item]): Vector[Slot[Item]] = v.map(toSlot)
  }
}