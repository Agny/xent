package ru.agny.xent.core.inventory

case class ItemHolder(slots: Vector[Slot[Item]]) extends SlotHolder[Item]
