package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.core.inventory._

case class EquippableHolder(set: EquipmentSet) extends SlotHolder[Equippable] {

  val items = Vector(set.mainHand, set.offHand, set.armor, set.accessory)
  val activeItems = Vector(set.mainHand, set.offHand)
  val passiveItems = Vector(set.armor, set.accessory)

  def set(idx: Int, v: Slot[Equippable])
         (implicit ev: ItemMerger[Equippable, Equippable]): (EquippableHolder, Slot[Equippable]) = {
    val (updatedSet, replaced) =
      if (idx < set.items.size) set.updateSlot(idx, v)
      else (set, v)
    if (updatedSet != set) (EquippableHolder(updatedSet), replaced)
    else (this, replaced)
  }

  def getIndexForEquippable(v: Equippable)(implicit ev: ItemMerger[Equippable, Equippable]): Int = set.optimalIndexFor(v)

  override val slots: Vector[Slot[Equippable]] = set.items
}
