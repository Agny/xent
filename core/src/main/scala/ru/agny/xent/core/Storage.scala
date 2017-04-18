package ru.agny.xent.core

import ru.agny.xent.Response
import ru.agny.xent.core.inventory._
import Item.ItemId

case class Storage(holder: ItemHolder) extends InventoryLike[Storage, Item] {

  import Item.implicits._
  import ItemMerger.implicits._

  val asInventory = this

  def tick(lastAction: Long, producers: Vector[Facility]): (Storage, Vector[Facility]) =
    producers.foldLeft(this, Vector.empty[Facility])((s, f) => {
      val (storage, updatedQueue) = f.tick(lastAction)(s._1)
      (storage, updatedQueue +: s._2)
    })

  def add(r: Vector[Item]): (Storage, Vector[Slot[Item]]) = r match {
    case h +: t =>
      val (store, remainder) = add(h)
      val (storeAcc, remainderAcc) = store.add(t)
      (storeAcc, remainder +: remainderAcc)
    case _ => (this, Vector.empty)
  }

  def spend(recipe: Cost): Either[Response, Storage] = {
    recipe.cost.find(x => !resources.exists(y => x.id == y.id && y.stackValue >= x.stackValue)) match {
      case Some(v) => Left(Response(s"There isn't enough of ${v.id}"))
      case None =>
        Right(Storage(recipe.cost.foldRight(resources)((a, b) => b.map(bb => bb.id match {
          case a.id => ResourceUnit(bb.stackValue - a.stackValue, a.id)
          case _ => bb
        }))))
    }
  }

  def get(resource: ItemId): Option[ResourceUnit] = resources.find(_.id == resource)

  def resources: Vector[ResourceUnit] = holder.slots.flatMap(x => x match {
    case ItemSlot(v) => v match {
      case ru: ResourceUnit => Some(ru)
      case _ => None
    }
    case EmptySlot => None
  })

  override def apply(slots: Vector[Slot[Item]]): Storage = Storage(slots)
}

object Storage {
  def empty: Storage = Storage(Vector.empty)

  def apply(slots: Vector[Slot[Item]]):Storage = Storage(ItemHolder(slots))
}
