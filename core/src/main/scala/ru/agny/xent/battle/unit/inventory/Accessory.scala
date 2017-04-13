package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.Property
import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core.ProductionSchema

trait Accessory extends Equippable

case object DefaultAccessory extends Accessory {
  override val id: ItemId = -1
  override val name = "Default"
  override val attrs: Seq[Property] = Seq.empty
  override val schema: ProductionSchema = ProductionSchema.default()
}
