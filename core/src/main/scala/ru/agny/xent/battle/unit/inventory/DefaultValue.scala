package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core.attributes.Blunt
import ru.agny.xent.battle.core.{Offensive, Dice, Property}
import ru.agny.xent.battle.core.Dice._
import ru.agny.xent.core.{Item, ProductionSchema}
import ru.agny.xent.core.Item._

trait DefaultValue[T] extends Item {
  this: T =>
  val self: T = this
}

object DefaultValue {
  object implicits {
    implicit object DefaultAccessory extends Accessory with DefaultValue[Accessory] {
      override val id: ItemId = -1
      override val name = "Default"
      override val attrs: Vector[Property] = Vector.empty
      override val schema: ProductionSchema = ProductionSchema.default()
    }

    implicit object DefaultArmor extends Armor with DefaultValue[Armor] {
      override val id: ItemId = -1
      override val value: Int = 0
      override val attrs: Vector[Property] = Vector.empty
      override val name = "Default"
      override val schema: ProductionSchema = ProductionSchema.default()
    }

    implicit object DefaultWeapon extends Weapon with DefaultValue[Weapon] {
      override val id: ItemId = -1
      val damage: Dice = 1 d 2
      override val name = "Unarmed"
      override val schema: ProductionSchema = ProductionSchema.default()
    }
  }
}