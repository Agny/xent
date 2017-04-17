package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.Dice

trait Weapon extends Equippable {
  val damage: Dice
}