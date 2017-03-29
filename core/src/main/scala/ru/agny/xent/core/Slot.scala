package ru.agny.xent.core

trait Slot {
  type InOut = (Slot, Slot)

  def get: Item

  def set(v: Item): InOut = (v, this) match {
    case stack@(toSet: StackableItem, ItemSlot(ths: StackableItem)) if toSet.id == ths.id =>
      (ItemSlot(ResourceUnit(ths.stackValue + toSet.stackValue, ths.id)), EmptySlot)
    case replace@(a, b) => (ItemSlot(a), b)
  }
}
object Slot {
  def empty = EmptySlot
}

case class ItemSlot(v: Item) extends Slot {
  def get = v
}

case object EmptySlot extends Slot {
  def get = throw new NoSuchElementException("EmptySlot.get")
}
