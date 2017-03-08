package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.{Property, Dice}
import ru.agny.xent.battle.core.Dice._

trait Weapon extends Equippable {
  val damage: Dice
}

case object DefaultWeapon extends Weapon {
  val damage: Dice =  1 d 2
  val attrs: Seq[Property] = Seq.empty
  val name: String = "Unarmed"
}