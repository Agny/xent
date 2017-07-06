package ru.agny.xent.core.unit.equip

import Dice._
import ru.agny.xent.core.Item._
import ru.agny.xent.core.{Item, ProductionSchema}

trait DefaultValue[T] extends Item {
  this: T =>
  val self: T = this
}

object DefaultValue {
  object implicits {
    implicit object DefaultAccessory extends Accessory with DefaultValue[Accessory] {
      override val id: ItemId = -1
      override val name = "Default"
      override val attrs: Vector[AttrProperty] = Vector.empty
      override val schema: ProductionSchema = ProductionSchema.default()
      override val weight: Int = 1
    }

    implicit object DefaultArmor extends Armor with DefaultValue[Armor] {
      override val id: ItemId = -1
      override val value: Int = 0
      override val attrs: Vector[AttrProperty] = Vector.empty
      override val name = "Default"
      override val schema: ProductionSchema = ProductionSchema.default()
      override val weight: Int = 1
    }

    implicit object DefaultWeapon extends Weapon with DefaultValue[Weapon] {
      override val id: ItemId = -1
      val damage: Dice = 1 d 2
      override val name = "Unarmed"
      override val schema: ProductionSchema = ProductionSchema.default()
      override val weight: Int = 1
    }
  }
}