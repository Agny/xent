package ru.agny.xent.core.city

import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core._
import ru.agny.xent.core.inventory._
import ru.agny.xent.messages.PlainResponse

case class Storage(holder: ItemHolder) extends InventoryLike[Storage, Item] {

  import Item.implicits._
  import ItemMerger.implicits._

  val asInventory = this

  def tick(period: ProgressTime, producers: Vector[Building]): (Storage, Vector[Building]) =
    producers.foldLeft(this, Vector.empty[Building])((s, f) => {
      val (building, items) = f.tick(period)
      val (storage, notAdded) = s._1.add(items)
      (storage, building +: s._2)
    })

  def add(r: Vector[Item]): (Storage, Vector[Slot[Item]]) = r match {
    case h +: t =>
      val (store, remainder) = add(h)
      val (storeAcc, remainderAcc) = store.add(t)
      (storeAcc, remainder +: remainderAcc)
    case _ => (this, Vector.empty)
  }

  def spend(recipe: Cost): Either[PlainResponse, Storage] = {
    recipe.v.find(x => !resources.exists(y => x.id == y.id && y.stackValue >= x.stackValue)) match {
      case Some(v) => Left(PlainResponse(s"There isn't enough of ${v.id}"))
      case None =>
        Right(Storage(recipe.v.foldRight(resources)((a, b) => b.map(bb => bb.id match {
          case a.id => ItemStack(bb.stackValue - a.stackValue, a.id)
          case _ => bb
        }))))
    }
  }

  def get(resource: ItemId): Option[ItemStack] = resources.find(_.id == resource)

  def resources: Vector[ItemStack] = holder.slots.flatMap(x => x match {
    case ItemSlot(v) => v match {
      case ru: ItemStack => Some(ru)
      case _ => None
    }
    case EmptySlot => None
  })

  override def apply(slots: Vector[Slot[Item]]): Storage = Storage(slots)
}

object Storage {
  val empty: Storage = Storage(Vector.empty)

  def apply(slots: Vector[Slot[Item]]): Storage = Storage(ItemHolder(slots))
}
