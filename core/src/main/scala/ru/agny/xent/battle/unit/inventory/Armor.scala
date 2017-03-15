package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.Property
import ru.agny.xent.core.ProductionSchema

trait Armor extends Equippable {
  val value: Int
}

case object DefaultArmor extends Armor {
  override val value: Int = 0
  override val attrs: Seq[Property] = Seq.empty
  override val name = "Default"
  override val schema: ProductionSchema = ProductionSchema.default()
}
