package ru.agny.xent.core

import ru.agny.xent.Response
import ru.agny.xent.core.Item.ItemId

case class Storage(slots: Vector[Slot[Item]]) extends InventoryLike[Storage, Item] {

  import Item.implicits._

  override implicit val s: Storage = this

  def tick(lastAction: Long, producers: Vector[Facility]): (Storage, Vector[Facility]) =
    producers.foldLeft(this, Vector.empty[Facility])((s, f) => {
      val (storage, updatedQueue) = f.tick(lastAction)(s._1)
      (storage, updatedQueue +: s._2)
    })

  def add(r: Vector[Item]): (Storage, Vector[Slot[Item]]) = r match {
    case h +: t =>
      val (store, remainder) = add(h)(s)
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

  override def isMoveAcceptable[U <: Item](v: U): Boolean = v match {
    case i: Item => true
    case _ => false
  }

  override def apply(slots: Vector[Slot[Item]]): Storage = Storage(slots)
}

object Storage {
  def empty: Storage = Storage(Vector.empty)
}
