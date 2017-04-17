package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.Property
import ru.agny.xent.core.Producible

trait Equippable extends Producible {
  val name: String
  val attrs: Vector[Property]
}