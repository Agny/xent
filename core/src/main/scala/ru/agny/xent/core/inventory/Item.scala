package ru.agny.xent.core.inventory

import ru.agny.xent.core.inventory.Item.{ItemId, ItemWeight}
import ru.agny.xent.core.inventory.Progress.ProgressTime

trait Item {
  val id: ItemId
  val weight: ItemWeight
}
trait DelayableItem extends Item {
  val name: String
  val yieldTime: ProgressTime
}

final case class ItemStack(stackValue: Int, id: ItemId, singleWeight: ItemWeight) extends Item {
  val weight = singleWeight * stackValue

  def add(v: ItemStack): ItemStack =
    if (v.id == id) ItemStack(v.stackValue + stackValue, id, singleWeight)
    else this
}

object Progress {
  type ProgressTime = Long
}
object Item {
  type ItemId = Long
  type ItemWeight = Int

  object implicits {
    implicit def convert(v: Item): Slot[Item] = v match {
      case ItemStack(stackValue, _, _) if stackValue <= 0 => EmptySlot
      case x => ItemSlot(x)
    }

    implicit def convert(v: Vector[Item])(implicit toSlot: Item => Slot[Item]): Vector[Slot[Item]] = v.map(toSlot)
  }
}