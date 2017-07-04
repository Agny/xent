package ru.agny.xent.battle

import ru.agny.xent.core.Item._
import ru.agny.xent.core.unit.equip._
import ru.agny.xent.core.unit.equip.attributes.{Piercing, Slashing}
import ru.agny.xent.core.{Cost, ProductionSchema}

package object unit {
  case class StubWeapon(id: ItemId = -1) extends Weapon {
    override val damage: Dice = Dice(1, 1)
    override val attrs: Vector[Property] = Vector(Property(Slashing, 10, Offensive), Property(Slashing, 1, Defensive))
    override val name: String = "knife"
    override val schema: ProductionSchema = ProductionSchema(0, Cost(Vector.empty), Set.empty)
    override val weight: Int = 0
  }
  case class StubArmor() extends Armor {
    override val attrs: Vector[Property] = Vector(Property(Slashing, 4, Defensive), Property(Piercing, 6, Defensive))
    override val name: String = "leather jacket"
    override val schema: ProductionSchema = ProductionSchema(0, Cost(Vector.empty), Set.empty)
    override val id: ItemId = -1
    override val value: Int = 0
    override val weight: Int = 0
  }
  case class StubAccessory() extends Accessory {
    override val attrs: Vector[Property] = Vector(Property(Slashing, 1, Offensive), Property(Slashing, 1, Defensive), Property(Piercing, 1, Defensive))
    override val name: String = "guard charm"
    override val schema: ProductionSchema = ProductionSchema(0, Cost(Vector.empty), Set.empty)
    override val id: ItemId = -1
    override val weight: Int = 0
  }

}
