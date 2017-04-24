package ru.agny.xent.battle.unit.inventory

import org.scalatest.{EitherValues, FlatSpec, Matchers}
import ru.agny.xent.battle.unit.helperClasses.StubWeapon
import ru.agny.xent.core.ResourceUnit
import ru.agny.xent.core.inventory.{EmptySlot, ItemSlot}

class BackpackTest extends FlatSpec with Matchers with EitherValues {

  "Backpack" should "add item" in {
    val bp = Backpack.empty
    val singleItem = ResourceUnit(1, 1)
    val (res, empty) = bp.add(Vector(singleItem))
    res.holder.slots should be(Vector(ItemSlot(singleItem)))
    empty should be(Vector(EmptySlot))
  }

  it should "stack consequently added resource items with same id" in {
    val bp = Backpack.empty
    val firstItem = Vector(ResourceUnit(1, 1))
    val secondItem = Vector(ResourceUnit(3, 1))
    val (bp1, _) = bp.add(firstItem)
    val (res, _) = bp1.add(secondItem)
    res.holder.slots should be(Vector(ItemSlot(ResourceUnit(4, 1))))
  }

  it should "not stack resource items with different ids" in {
    val bp = Backpack.empty
    val firstItem = ResourceUnit(1, 1)
    val secondItem = ResourceUnit(3, 2)
    val (res, _) = bp.add(Vector(firstItem, secondItem))
    res.holder.slots should be(Vector(ItemSlot(secondItem), ItemSlot(firstItem)))
  }

  it should "not stack simple items" in {
    val bp = Backpack.empty
    val firstItem = StubWeapon()
    val secondItem = StubWeapon()
    val (res, _) = bp.add(Vector(firstItem, secondItem))
    res.holder.slots should be(Vector(ItemSlot(secondItem), ItemSlot(firstItem)))
  }

  it should "add items of several types" in {
    val bp = Backpack(Vector(ItemSlot(ResourceUnit(2, 1))))
    val firstItem = ResourceUnit(1, 1)
    val secondItem = StubWeapon()
    val (res, _) = bp.add(Vector(firstItem, secondItem))
    res.holder.slots should be(Vector(ItemSlot(secondItem), ItemSlot(ResourceUnit(3, 1))))
  }
}