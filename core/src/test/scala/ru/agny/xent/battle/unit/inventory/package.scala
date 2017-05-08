package ru.agny.xent.battle.unit

import ru.agny.xent.battle.core.attributes.{Piercing, Slashing}
import ru.agny.xent.battle.core.{Defensive, Dice, Offensive, Property}
import ru.agny.xent.battle.unit.inventory.{Accessory, Armor, Weapon}
import ru.agny.xent.core.Item._
import ru.agny.xent.core.ProductionSchema

package object helperClasses {
  case class StubWeapon(id: ItemId = -1) extends Weapon {
    override val damage: Dice = Dice(1, 1)
    override val attrs: Vector[Property] = Vector(Property(Slashing, 10, Offensive), Property(Slashing, 1, Defensive))
    override val name: String = "knife"
    override val schema: ProductionSchema = ProductionSchema(0, Vector.empty, Set.empty)
  }
  case class StubArmor() extends Armor {
    override val attrs: Vector[Property] = Vector(Property(Slashing, 4, Defensive), Property(Piercing, 6, Defensive))
    override val name: String = "leather jacket"
    override val schema: ProductionSchema = ProductionSchema(0, Vector.empty, Set.empty)
    override val id: ItemId = -1
    override val value: Int = 0
  }
  case class StubAccessory() extends Accessory {
    override val attrs: Vector[Property] = Vector(Property(Slashing, 1, Offensive), Property(Slashing, 1, Defensive), Property(Piercing, 1, Defensive))
    override val name: String = "guard charm"
    override val schema: ProductionSchema = ProductionSchema(0, Vector.empty, Set.empty)
    override val id: ItemId = -1
  }

}