package ru.agny.xent.battle

import ru.agny.xent.core.inventory.Item._
import ru.agny.xent.core.unit.equip._
import ru.agny.xent.core.unit.equip.attributes.{Piercing, Slashing}
import ru.agny.xent.core.inventory.{Cost, ProductionSchema}

package object unit {
  case class StubWeapon(id: ItemId = -1) extends Weapon {
    override val damage: Dice = Dice(1, 1)
    override val attrs: Vector[AttrProperty] = Vector(AttrProperty(Slashing, 10, Offensive), AttrProperty(Slashing, 1, Defensive))
    override val name: String = "knife"
    override val schema: ProductionSchema = ProductionSchema(0, Cost(Vector.empty), 100, Set.empty)
    override val battleRate: Int = 0
  }
  case class StubStrongWeapon(id: ItemId = -1) extends Weapon {
    override val damage: Dice = Dice(100, 10)
    override val attrs: Vector[AttrProperty] = Vector(AttrProperty(Slashing, 10, Offensive), AttrProperty(Slashing, 1, Defensive))
    override val name: String = "slasher3000"
    override val schema: ProductionSchema = ProductionSchema(0, Cost(Vector.empty), 100, Set.empty)
    override val battleRate: Int = 0
  }
  case class StubArmor() extends Armor {
    override val attrs: Vector[AttrProperty] = Vector(AttrProperty(Slashing, 4, Defensive), AttrProperty(Piercing, 6, Defensive))
    override val name: String = "leather jacket"
    override val schema: ProductionSchema = ProductionSchema(0, Cost(Vector.empty), 100, Set.empty)
    override val id: ItemId = -1
    override val value: Int = 3
    override val battleRate: Int = 0
  }
  case class StubAccessory() extends Accessory {
    override val attrs: Vector[AttrProperty] = Vector(AttrProperty(Slashing, 1, Offensive), AttrProperty(Slashing, 1, Defensive), AttrProperty(Piercing, 1, Defensive))
    override val name: String = "guard charm"
    override val schema: ProductionSchema = ProductionSchema(0, Cost(Vector.empty), 100, Set.empty)
    override val id: ItemId = -1
    override val battleRate: Int = 0
  }

}
