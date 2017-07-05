package ru.agny.xent.core.unit.equip

import ru.agny.xent.core.Producible

trait Equippable extends Producible {
  val name: String
  val attrs: Vector[AttrProperty]
  val weight: Int
}