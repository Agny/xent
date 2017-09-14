package ru.agny.xent.core.inventory

trait SlotHolder[T <: Item] {
  val slots: Vector[Slot[T]]
}
