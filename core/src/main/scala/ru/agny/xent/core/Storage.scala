package ru.agny.xent.core

import ru.agny.xent.Response
import ru.agny.xent.core.Item.ItemId

case class Storage(slots: Seq[Slot[Item]]) extends InventoryLike[Storage, Item] {

  import Item.implicits._

  override implicit val s: Storage = this

  def tick(lastAction: Long, producers: Seq[Facility]): (Storage, Seq[Facility]) =
    producers.foldLeft(this, Seq.empty[Facility])((s, f) => {
      val (storage, updatedQueue) = f.tick(lastAction)(s._1)
      (storage, s._2 :+ updatedQueue)
    })

  def add(r: Seq[Item]): (Storage, Seq[Slot[Item]]) = r match {
    case Seq(h, t@_*) =>
      val (store, remainder) = add(h)(s)
      val (storeAcc, remainderAcc) = store.add(t)
      (storeAcc, remainderAcc :+ remainder)
    case _ => (this, Seq.empty)
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

  override def apply(slots: Seq[Slot[Item]]): Storage = Storage(slots)
}

object Storage {
  def empty: Storage = Storage(Seq.empty)
}
