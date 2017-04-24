package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.core.inventory._

case class EquippableHolder(set: EquipmentSet) extends SlotHolder[Equippable] {

  val items = Vector(set.mainHand, set.offHand, set.armor, set.accessory)
  val activeItems = Vector(set.mainHand, set.offHand)
  val passiveItems = Vector(set.armor, set.accessory)

  def set(idx: Int, v: Slot[Equippable])
         (implicit ev: ItemMerger[Equippable, Equippable]): (EquippableHolder, Slot[Equippable]) = {
    val (updatedSet, replaced) = idx match {
      case Equipment.mainWeaponIdx => set.updateSlot(idx, set.mainHand, v)
      case Equipment.secondaryWeaponIdx => set.updateSlot(idx, set.offHand, v)
      case Equipment.armorIdx => set.updateSlot(idx, set.armor, v)
      case Equipment.accessoryIdx => set.updateSlot(idx, set.accessory, v)
      case _ => (set, v)
    }
    if (updatedSet != set) (EquippableHolder(updatedSet), replaced)
    else (this, replaced)
  }

  override val slots: Vector[Slot[Equippable]] = set.items
}
