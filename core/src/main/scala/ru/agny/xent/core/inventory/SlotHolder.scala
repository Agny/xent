package ru.agny.xent.core.inventory

import ru.agny.xent.core.Item

trait SlotHolder[T <: Item] {
  val slots: Vector[Slot[T]]
}
