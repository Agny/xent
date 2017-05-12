package ru.agny.xent.battle.unit.inventory

import org.scalatest.{EitherValues, FlatSpec, Matchers}
import ru.agny.xent.battle.core.attributes.{Piercing, Slashing}
import ru.agny.xent.battle.core.{Defensive, Offensive, Property}
import ru.agny.xent.battle.unit.helperClasses.{StubAccessory, StubArmor, StubWeapon}
import ru.agny.xent.battle.unit.inventory.DefaultValue.implicits.DefaultWeapon
import ru.agny.xent.core.Storage
import ru.agny.xent.core.inventory.{EmptySlot, ItemSlot}

import scala.collection.immutable.Vector

class EquipmentTest extends FlatSpec with Matchers with EitherValues {

  import Equipment._
  import ru.agny.xent.core.inventory.ItemMerger.implicits._
  import ru.agny.xent.core.inventory.ItemSubTyper.implicits._
  import ru.agny.xent.core.inventory.ItemLike.implicits._

  "Equipment" should "set main weapon" in {
    val eq = Equipment.empty
    val wpn = StubWeapon(1)
    val (res, empty) = eq.set(mainWeaponIdx, ItemSlot(wpn))
    res.weapons should be(Vector(wpn, DefaultWeapon))
    res.holder.set.mainHand should be(wpn)
    empty should be(EmptySlot)
  }

  it should "set secondary weapon" in {
    val eq = Equipment.empty
    val wpn = StubWeapon(1)
    val (res, empty) = eq.set(secondaryWeaponIdx, ItemSlot(wpn))
    res.weapons should be(Vector(DefaultWeapon, wpn))
    res.holder.set.offHand should be(wpn)
    empty should be(EmptySlot)
  }

  it should "set armor" in {
    val eq = Equipment.empty
    val armor = StubArmor()
    val (res, empty) = eq.set(armorIdx, ItemSlot(armor))
    res.armor should be(armor)
    empty should be(EmptySlot)
  }

  it should "set accessory" in {
    val eq = Equipment.empty
    val accessory = StubAccessory()
    val (res, empty) = eq.set(accessoryIdx, ItemSlot(accessory))
    res.accessory should be(accessory)
    empty should be(EmptySlot)
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

  it should "handle equipment from storage" in {
    val mh = StubWeapon(1)
    val armor = StubArmor()
    val storage = Storage(Vector(ItemSlot(mh), ItemSlot(armor)))
    val eq = Equipment(Vector.empty)
    val (ustorage, ueq) = storage.move(0, eq)
    val (resStorage, resEq) = ustorage.move(0, ueq)
    resStorage.holder.slots should be(Vector.empty)
    resEq.weapons should contain(mh)
    resEq.armor should be(armor)
  }

  it should "fill weapons in unarmed slots from storage" in {
    val wpn1 = StubWeapon(1)
    val wpn2 = StubWeapon(2)
    val storage = Storage(Vector(ItemSlot(wpn1), ItemSlot(wpn2)))
    val eq = Equipment(Vector.empty)
    val (ustorage, ueq) = storage.move(0, eq)
    val (resStorage, resEq) = ustorage.move(0, ueq)
    resStorage.holder.slots should be(Vector.empty)
    resEq.weapons should be(Vector(wpn1, wpn2))
  }

  it should "take off weapon if needed" in {
    val wpn = StubWeapon(1)
    val eq = Equipment(Vector(ItemSlot(wpn)))
    val (res, oldWpn) = eq.set(0, EmptySlot)
    res.weapons should be(Vector(DefaultWeapon, DefaultWeapon))
    oldWpn.get should be(wpn)
  }

  it should "return same equipment object if there were no change" in {
    val equip = Equipment(Vector.empty)
    val (res, _) = equip.set(0, EmptySlot)
    res should be theSameInstanceAs equip
  }
}

