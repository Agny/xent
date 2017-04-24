package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.core.inventory._

case class EquipmentSet(private val slots: Vector[Slot[Equippable]]) {

  import DefaultValue.implicits._
  import EquipmentSet._
  import ItemSubChecker.implicits._

  val mainHand = extractValue[Weapon](slots, Equipment.mainWeaponIdx)
  val offHand = extractValue[Weapon](slots, Equipment.secondaryWeaponIdx)
  val armor = extractValue[Armor](slots, Equipment.armorIdx)
  val accessory = extractValue[Accessory](slots, Equipment.accessoryIdx)

  val items = Vector(ItemSlot(mainHand), ItemSlot(offHand), ItemSlot(armor), ItemSlot(accessory))

  def updateSlot[T <: Equippable](idx: Int, current: T, toSet: Slot[T])
                                 (implicit ev: ItemMerger[T, T]): (EquipmentSet, Slot[T]) = {
    toSet match {
      case i@ItemSlot(iv) => ItemSlot(current).set(iv) match {
        case Some((newValue, old)) => (EquipmentSet(items.updated(idx, newValue)), old)
        case None => (this, toSet)
      }
      case s@EmptySlot => (EquipmentSet(items.updated(idx, s)), ItemSlot(current))
    }
  }
}

object EquipmentSet {
  private def extractValue[T <: Equippable](v: Vector[Slot[Equippable]], idx: Int)
                                           (implicit ev: DefaultValue[T], ev2: ItemSubChecker[Equippable, T]): T = getEquip[T](v, idx) match {
    case Some(i) => i
    case _ => ev.self
  }

  private def getEquip[T <: Equippable](v: Vector[Slot[Equippable]], idx: Int)
                                       (implicit matcher: ItemSubChecker[Equippable, T]): Option[T] = {
    if (idx < v.length && !v(idx).isEmpty) {
      matcher.asSub(v(idx).get) match {
        case a@Some(x) => a
        case _ => None
      }
    } else None
  }
}
