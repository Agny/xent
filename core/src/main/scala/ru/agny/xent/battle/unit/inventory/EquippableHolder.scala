package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.core.inventory._

case class EquippableHolder(slots: Vector[Slot[Equippable]]) extends SlotHolder[Equippable] {

  import DefaultValue.implicits._
  import EquippableHolder._
  import ItemSubChecker.implicits._

  val mainHand = extractValue[Weapon](slots)
  val offHand = extractValue[Weapon](slots.diff(Vector(ItemSlot(mainHand))))
  val armor = extractValue[Armor](slots)
  val accessory = extractValue[Accessory](slots)

  val items = Vector(mainHand, offHand, armor, accessory)
  val activeItems = Vector(mainHand, offHand)
  val passiveItems = Vector(armor, accessory)

  def set(idx: Int, v: Slot[Equippable])
         (implicit ev: ItemMerger[Equippable, Equippable]): (EquippableHolder, Slot[Equippable]) = {
    val (updated, replaced) = idx match {
      case 0 => updateSlot(mainHand, DefaultWeapon, v)
      case 1 => updateSlot(offHand, DefaultWeapon, v)
      case 2 => updateSlot(armor, DefaultArmor, v)
      case 3 => updateSlot(accessory, DefaultAccessory, v)
      case _ => (EmptySlot, v)
    }
    val replacedIdx = slots.indexOf(replaced)
    if (replacedIdx != -1) {
      (EquippableHolder(slots.updated(replacedIdx, updated)), replaced)
    } else {
      (this, replaced)
    }
  }
}

object EquippableHolder {
  private def extractValue[T <: Equippable](v: Vector[Slot[Equippable]])
                                           (implicit ev: DefaultValue[T], ev2: ItemSubChecker[Equippable, T]): T = getEquip[T](v) match {
    case x +: xs => x
    case _ => ev.self
  }

  private def getEquip[T <: Equippable](v: Vector[Slot[Equippable]])
                                       (implicit matcher: ItemSubChecker[Equippable, T]): Vector[T] =
    v.filter(s => !s.isEmpty).flatMap(s => matcher.asSub(s.get) match {
      case a@Some(x) => a
      case _ => None
    })

  private def updateSlot[T <: Equippable](current: T, default: T, toSet: Slot[T])
                                         (implicit ev: ItemMerger[T, T]): (Slot[T], Slot[T]) = toSet match {
    case i@ItemSlot(iv) => ItemSlot(current).set(iv) match {
      case Some((newValue, old)) => (newValue, old)
      case None => (ItemSlot(current), toSet)
    }
    case EmptySlot => (ItemSlot(default), ItemSlot(current))
  }

}
