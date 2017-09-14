package ru.agny.xent.battle.unit

import ru.agny.xent.core.inventory._

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

  def toSpoil: Vector[Item] = holder.slots.flatMap(_.flatten)

  override def apply(slots: Vector[Slot[Item]]): Backpack = Backpack(slots)
}

object Backpack {
  val empty: Backpack = Backpack(Vector.empty)

  def apply(slots: Vector[Slot[Item]]): Backpack = Backpack(ItemHolder(slots))
}