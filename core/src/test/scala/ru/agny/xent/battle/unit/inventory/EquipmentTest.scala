package ru.agny.xent.battle.unit.inventory

import org.scalatest.{EitherValues, FlatSpec, Matchers}
import ru.agny.xent.battle.core.attributes.{Piercing, Slashing}
import ru.agny.xent.battle.core.{Defensive, Offensive, Property}
import ru.agny.xent.battle.unit.helperClasses.{StubAccessory, StubArmor, StubWeapon}
import ru.agny.xent.battle.unit.inventory.DefaultValue.implicits.{DefaultAccessory, DefaultArmor, DefaultWeapon}
import ru.agny.xent.core.inventory.{EmptySlot, ItemSlot}

import scala.collection.immutable.Vector

class EquipmentTest extends FlatSpec with Matchers with EitherValues {

  import Equipment._

  "Equipment" should "set main weapon" in {
    val eq = Equipment.empty
    val wpn = StubWeapon(1)
    val (res, empty) = eq.set(mainWeaponIdx, ItemSlot(wpn))
    res.weapons should be(Vector(wpn, DefaultWeapon))
    res.holder.set.mainHand should be(wpn)
    empty should be(ItemSlot(DefaultWeapon))
  }

  it should "set secondary weapon" in {
    val eq = Equipment.empty
    val wpn = StubWeapon(1)
    val (res, empty) = eq.set(secondaryWeaponIdx, ItemSlot(wpn))
    res.weapons should be(Vector(DefaultWeapon, wpn))
    res.holder.set.offHand should be(wpn)
    empty should be(ItemSlot(DefaultWeapon))
  }

  it should "set armor" in {
    val eq = Equipment.empty
    val armor = StubArmor()
    val (res, empty) = eq.set(armorIdx, ItemSlot(armor))
    res.armor should be(armor)
    res.holder.set.armor should be(armor)
    empty should be(ItemSlot(DefaultArmor))
  }

  it should "set accessory" in {
    val eq = Equipment.empty
    val accessory = StubAccessory()
    val (res, empty) = eq.set(accessoryIdx, ItemSlot(accessory))
    res.accessory should be(accessory)
    res.holder.set.accessory should be(accessory)
    empty should be(ItemSlot(DefaultAccessory))
  }

  it should "replace main weapon if slot isn't empty" in {
    val wpn = StubWeapon(1)
    val toSet = StubWeapon(2)
    val eq = Equipment(Vector(ItemSlot(wpn)))
    val (res, old) = eq.set(mainWeaponIdx, ItemSlot(toSet))
    res.weapons should be(Vector(toSet, DefaultWeapon))
    res.holder.set.mainHand should be(toSet)
    old should be(ItemSlot(wpn))
  }

  it should "replace secondary weapon if slot isn't empty" in {
    val wpn = StubWeapon(1)
    val toSet = StubWeapon(2)
    val eq = Equipment.empty
    val (withOh, _) = eq.set(secondaryWeaponIdx, ItemSlot(wpn))
    val (res, old) = withOh.set(secondaryWeaponIdx, ItemSlot(toSet))
    res.weapons should be(Vector(DefaultWeapon, toSet))
    res.holder.set.offHand should be(toSet)
    old should be(ItemSlot(wpn))
  }

  it should "place all items" in {
    val mh = StubWeapon(1)
    val oh = StubWeapon(2)
    val armor = StubArmor()
    val accessory = StubAccessory()
    val items = Vector(ItemSlot(mh), ItemSlot(oh), ItemSlot(armor), ItemSlot(accessory))
    val res = Equipment(items)
    res.weapons should be(Vector(mh, oh))
    res.armor should be(armor)
    res.accessory should be(accessory)
  }

  it should "ignore indices greater than 3" in {
    val eq = Equipment.empty
    val wpn = StubWeapon(1)
    val nonExistedSlot = EmptySlot
    val (withOh, _) = eq.set(secondaryWeaponIdx, ItemSlot(wpn))
    val (res, old) = withOh.set(4, nonExistedSlot)
    res.weapons should be(Vector(DefaultWeapon, wpn))
    res.holder.set.offHand should be(wpn)
    old should be(nonExistedSlot)
  }

  it should "collect defense attributes" in {
    val mh = StubWeapon(1)
    val oh = StubWeapon(2)
    val armor = StubArmor()
    val accessory = StubAccessory()
    val items = Vector(ItemSlot(mh), ItemSlot(oh), ItemSlot(armor), ItemSlot(accessory))
    val eq = Equipment(items)
    val res = eq.props(mh)(Defensive)
    val expected = Vector(Property(Slashing, 7, Defensive), Property(Piercing, 7, Defensive))
    res should be(expected)
  }

  it should "collect offense attributes" in {
    val mh = StubWeapon(1)
    val oh = StubWeapon(2)
    val armor = StubArmor()
    val accessory = StubAccessory()
    val items = Vector(ItemSlot(mh), ItemSlot(oh), ItemSlot(armor), ItemSlot(accessory))
    val eq = Equipment(items)
    val res = eq.props(mh)(Offensive)
    val expected = Vector(Property(Slashing, 12, Offensive))
    res should be(expected)
  }

  //  it should "handle equipment from storage" in {
  //    val itemId = 1
  //    val mh = StubWeapon(itemId)
  //    val storage = Storage(Vector(ItemSlot(mh)))
  //    val eq = Equipment(Vector.empty)
  //    val (res, _) = eq.set(0, storage.getSlot(itemId))
  //    res.holder.set.mainHand should be(mh)
  //  }
}

