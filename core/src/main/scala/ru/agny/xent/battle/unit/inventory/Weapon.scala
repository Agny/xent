package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.attributes.Blunt
import ru.agny.xent.battle.core.{Offensive, Property, Dice}

trait Weapon extends Equippable {
  val damage: Dice
  val attrs = Vector(Property(Blunt, 1, Offensive))
}