package ru.agny.xent.core

import ru.agny.xent.core.inventory.{SlotHolder, Slot}

case class ItemHolder(slots: Vector[Slot[Item]]) extends SlotHolder[Item]
