package ru.agny.xent.core

sealed trait Slot[+T <: Item] {
  def get: T

  def isEmpty: Boolean

  def set[U <: Item](v: U): (Slot[U], Slot[T]) = (v, this) match {
    case stack@(toSet: StackableItem, ItemSlot(ths: StackableItem)) if toSet.id == ths.id =>
      (ItemSlot(ResourceUnit(ths.stackValue + toSet.stackValue, ths.id).asInstanceOf[U]), EmptySlot)
    case replace@(a, b) => (ItemSlot(a), b)
  }
}
object Slot {
  def empty = EmptySlot
}

final case class ItemSlot[+T <: Item](v: T) extends Slot[T] {
  def get = v

  def isEmpty = false
}

case object EmptySlot extends Slot[Nothing] {
  def get = throw new NoSuchElementException("EmptySlot.get")

  def isEmpty = true
}
