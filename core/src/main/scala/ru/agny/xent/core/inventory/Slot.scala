package ru.agny.xent.core.inventory

import ru.agny.xent.core.Item

sealed trait Slot[+T <: Item] {
  def get: T

  def isEmpty: Boolean

  def set[U <: Item](v: U)(implicit ev: ItemMerger[T, U]): (Slot[U], Slot[T]) = ev.asCompatible(get, v) match {
    case Some(x) => (ItemSlot(x), EmptySlot)
    case None => (ItemSlot(v), this)
  }
}
object Slot {
  implicit def convert[To <: Item, From <: Item](v: Slot[From])(implicit ev: ItemLike[To, From]): Slot[To] = v match {
    case ItemSlot(x) => ev.cast(x) match {
      case (Some(a), Some(b)) => ItemSlot(b)
      case _ => EmptySlot
    }
    case _ => EmptySlot
  }
}

final case class ItemSlot[T <: Item](v: T) extends Slot[T] {
  def get = v

  def isEmpty = false
}

case object EmptySlot extends Slot[Nothing] {
  def get = throw new NoSuchElementException("EmptySlot.get")

  def isEmpty = true
}
