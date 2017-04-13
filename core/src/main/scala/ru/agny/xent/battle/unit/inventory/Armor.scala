package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.Property
import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core.ProductionSchema

trait Armor extends Equippable {
  val value: Int
}

case object DefaultArmor extends Armor {
  override val id: ItemId = -1
  override val value: Int = 0
  override val attrs: Vector[Property] = Vector.empty
  override val name = "Default"
  override val schema: ProductionSchema = ProductionSchema.default()
}
