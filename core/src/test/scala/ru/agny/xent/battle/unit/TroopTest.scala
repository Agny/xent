package ru.agny.xent.battle.unit

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.TestHelper
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.inventory.ItemSlot
import ru.agny.xent.core.unit.equip.Equipment
import ru.agny.xent.core.unit._
import ru.agny.xent.core.utils.NESeq

class TroopTest extends FlatSpec with Matchers with EitherValues {

  val soul1 = Soul(1, SoulData(Level(1, 0), Spirit(50, 0, 50), Stats.default, Vector.empty), Equipment.empty)
  val soul2 = Soul(2, SoulData(Level(2, 0), Spirit(60, 0, 60), Stats.default, Vector.empty), Equipment.empty)
  val soul3 = Soul(3, SoulData(Level(3, 0), Spirit(75, 0, 75), Stats.default, Vector.empty), Equipment.empty)
  val soul4 = Soul(4, SoulData(Level(4, 0), Spirit(75, 0, 75), Stats.default, Vector.empty), Equipment.empty)

  "Troop" should "damage first unit of other troop while using BasicTactic" in {
    val t1 = Troop(1, NESeq(Vector(soul1, soul2)), Backpack.empty, 1, Coordinate(1, 1))
    val t2 = Troop(2, NESeq(Vector(soul3, soul4)), Backpack.empty, 2, Coordinate(1, 1))
    val (_, t2u) = t1.attack(t2)
    val s3u = t2u.activeUnits.head
    val s4u = t2u.activeUnits.tail.head
    s3u.spirit.points should be < soul3.spirit.points
    s4u should be theSameInstanceAs soul4
  }

  it should "get spoils if enemy dies" in {
    val t1 = Troop(1, NESeq(Vector(soul1, soul2)), Backpack.empty, 1, Coordinate(1, 1))
    val spoils = ItemSlot(StubWeapon(1))
    val backpackLoot = ItemSlot(StubArmor())
    val t2 = Troop(2, NESeq(Vector(TestHelper.defaultSoul(1))), Backpack(Vector(backpackLoot)), 2, Coordinate(1, 1))
    val (t1u, t2u) = t1.attack(t2)
    t1u.backpack.holder.slots should be(Vector(spoils, backpackLoot))
    t2u.activeUnits should be(Vector.empty)
    t2u.backpack should be(Backpack.empty)
  }

}
