package ru.agny.xent.core

trait SlotHolder[T <: Item] {
  val slots: Vector[Slot[T]]
}
