package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.core.inventory.{InventoryLike, ItemMerger, Slot}
import ru.agny.xent.core.{Item, ItemHolder}

case class Backpack(holder: ItemHolder) extends InventoryLike[Backpack, Item] {

  import ItemMerger.implicits._

  val asInventory = this

  def add(r: Vector[Item]): (Backpack, Vector[Slot[Item]]) = r match {
    case h +: t =>
      val (store, remainder) = add(h)
      val (storeAcc, remainderAcc) = store.add(t)
      (storeAcc, remainder +: remainderAcc)
    case _ => (this, Vector.empty)
  }

  override def apply(slots: Vector[Slot[Item]]): Backpack = Backpack(slots)
}

object Backpack {
  def empty: Backpack = Backpack(Vector.empty)

  def apply(slots: Vector[Slot[Item]]): Backpack = Backpack(ItemHolder(slots))
}