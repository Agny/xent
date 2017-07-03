package ru.agny.xent.battle.unit

import org.scalatest.{EitherValues, FlatSpec, Matchers}
import ru.agny.xent.core.ItemStack
import ru.agny.xent.core.inventory.{EmptySlot, ItemSlot}

class BackpackTest extends FlatSpec with Matchers with EitherValues {

  "Backpack" should "add item" in {
    val bp = Backpack.empty
    val singleItem = ItemStack(1, 1)
    val (res, empty) = bp.add(Vector(singleItem))
    res.holder.slots should be(Vector(ItemSlot(singleItem)))
    empty should be(Vector(EmptySlot))
  }

  it should "stack consequently added resource items with same id" in {
    val bp = Backpack.empty
    val firstItem = Vector(ItemStack(1, 1))
    val secondItem = Vector(ItemStack(3, 1))
    val (bp1, _) = bp.add(firstItem)
    val (res, _) = bp1.add(secondItem)
    res.holder.slots should be(Vector(ItemSlot(ItemStack(4, 1))))
  }

  it should "not stack resource items with different ids" in {
    val bp = Backpack.empty
    val firstItem = ItemStack(1, 1)
    val secondItem = ItemStack(3, 2)
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
    val bp = Backpack(Vector(ItemSlot(ItemStack(2, 1))))
    val firstItem = ItemStack(1, 1)
    val secondItem = StubWeapon()
    val (res, _) = bp.add(Vector(firstItem, secondItem))
    res.holder.slots should be(Vector(ItemSlot(secondItem), ItemSlot(ItemStack(3, 1))))
  }
}