package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.Dice
import ru.agny.xent.battle.core.Dice._
import ru.agny.xent.core.ProductionSchema

trait Weapon extends Equippable {
  val damage: Dice
}

case object DefaultWeapon extends Weapon {
  val damage: Dice = 1 d 2
  val attrs = Seq.empty
  override val name = "Unarmed"
  override val schema: ProductionSchema = ProductionSchema.default()
}