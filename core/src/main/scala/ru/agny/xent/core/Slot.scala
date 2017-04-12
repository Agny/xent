package ru.agny.xent.core

sealed trait Slot[+T <: Item] {
  def get: T

  def isEmpty: Boolean

  def set[U <: Item](v: U)(implicit ev: ItemMatcher[T, U]): (Slot[U], Slot[T]) = ev.toStack(get, v) match {
    case Some(x) => (ItemSlot(x), EmptySlot)
    case None => (ItemSlot(v), this)
  }
}
object Slot {
  def empty = EmptySlot
}

final case class ItemSlot[T <: Item](v: T) extends Slot[T] {
  def get = v

  def isEmpty = false
}

case object EmptySlot extends Slot[Nothing] {
  def get = throw new NoSuchElementException("EmptySlot.get")

  def isEmpty = true
}
