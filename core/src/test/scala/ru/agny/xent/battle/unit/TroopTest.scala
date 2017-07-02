package ru.agny.xent.battle.unit

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.battle.unit.helperClasses.{StubArmor, StubWeapon}
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.inventory.ItemSlot
import ru.agny.xent.core.unit.equip.Equipment
import ru.agny.xent.core.unit.{LevelBar, SpiritBar, Soul}
import ru.agny.xent.core.utils.NESeq

class TroopTest extends FlatSpec with Matchers with EitherValues {

  val soul1 = Soul(1, LevelBar(1, 0, 0), SpiritBar(50, 0, 50), Equipment.empty, 10, Vector.empty)
  val soul2 = Soul(2, LevelBar(2, 0, 0), SpiritBar(60, 0, 60), Equipment.empty, 10, Vector.empty)
  val soul3 = Soul(3, LevelBar(3, 0, 0), SpiritBar(75, 0, 75), Equipment.empty, 10, Vector.empty)
  val soul4 = Soul(4, LevelBar(3, 0, 0), SpiritBar(75, 0, 75), Equipment.empty, 10, Vector.empty)

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
    val t2 = Troop(2, NESeq(Vector(Soul(1, LevelBar(1, 0, 0), SpiritBar(1, 0, 0), Equipment(Vector(spoils)), 10, Vector.empty))), Backpack(Vector(backpackLoot)), 2, Coordinate(1, 1))
    val (t1u, t2u) = t1.attack(t2)
    t1u.backpack.holder.slots should be(Vector(spoils, backpackLoot))
    t2u.activeUnits should be(Vector.empty)
    t2u.backpack should be(Backpack.empty)
  }

}
