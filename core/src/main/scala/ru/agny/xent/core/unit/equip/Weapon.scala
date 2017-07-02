package ru.agny.xent.core.unit.equip

import ru.agny.xent.core.unit.equip.attributes.Blunt

trait Weapon extends Equippable {
  val damage: Dice
  val attrs = Vector(Property(Blunt, 1, Offensive))
}