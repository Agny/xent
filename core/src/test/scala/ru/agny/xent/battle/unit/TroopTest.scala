package ru.agny.xent.battle.unit

import org.scalatest.{EitherValues, FlatSpec, Matchers}
import ru.agny.xent.TestHelper
import ru.agny.xent.battle.MovementPlan
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.inventory.ItemSlot
import ru.agny.xent.core.unit.equip.Equipment
import ru.agny.xent.core.unit._
import ru.agny.xent.core.utils.NESeq

class TroopTest extends FlatSpec with Matchers with EitherValues {

  val pos = MovementPlan.idle(Coordinate(1, 1))
  val soul1 = Soul(1, SoulData(Level(1, 0), 50, Stats.default, Vector.empty), Equipment.empty)
  val soul2 = Soul(2, SoulData(Level(2, 0), 60, Stats.default, Vector.empty), Equipment.empty)
  val soul3 = Soul(3, SoulData(Level(3, 0), 75, Stats.default, Vector.empty), Equipment.empty)
  val soul4 = Soul(4, SoulData(Level(4, 0), 75, Stats.default, Vector.empty), Equipment.empty)

  "Troop" should "damage first unit of other troop while using BasicTactic" in {
    val t1 = Troop(1, NESeq(Vector(soul1, soul2)), Backpack.empty, 1, pos)
    val t2 = Troop(2, NESeq(Vector(soul3, soul4)), Backpack.empty, 2, pos)
    val (_, t2u) = t1.attack(t2)
    val s3u = t2u.activeUnits.head
    val s4u = t2u.activeUnits.tail.head
    s3u.spirit.points should be < soul3.spirit.points
    s4u should be theSameInstanceAs soul4
  }

  it should "get spoils if enemy dies" in {
    val t1 = Troop(1, NESeq(Vector(soul1, soul2)), Backpack.empty, 1, pos)
    val spoils = ItemSlot(StubWeapon(1))
    val backpackLoot = ItemSlot(StubArmor())
    val t2 = Troop(2, NESeq(Vector(TestHelper.defaultSoul(1, Equipment.empty.add(spoils.get)._1))), Backpack(Vector(backpackLoot)), 2, pos)
    val (t1u, t2u) = t1.attack(t2)
    t1u.backpack.holder.slots should be(Vector(spoils, backpackLoot))
    t2u.activeUnits should be(Vector.empty)
    t2u.backpack should be(Backpack.empty)
  }

}
