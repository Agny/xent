package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.core.inventory._
import ru.agny.xent.core.utils.SubTyper

case class EquipmentSet(private val slots: Vector[Slot[Equippable]]) {

  import DefaultValue.implicits._
  import EquipmentSet._
  import ItemSubTyper.implicits._

  val mainHand = extractValue[Weapon](slots, Equipment.mainWeaponIdx)
  val offHand = extractValue[Weapon](slots, Equipment.secondaryWeaponIdx)
  val armor = extractValue[Armor](slots, Equipment.armorIdx)
  val accessory = extractValue[Accessory](slots, Equipment.accessoryIdx)

  val items = Vector(ItemSlot(mainHand), ItemSlot(offHand), ItemSlot(armor), ItemSlot(accessory))

  def updateSlot[T <: Equippable](idx: Int, toSet: Slot[T])
                                 (implicit ev: ItemMerger[Equippable, T]): (EquipmentSet, Slot[Equippable]) = {
    val defaultsExcluded = items(idx) match {
      case ItemSlot(DefaultWeapon | DefaultArmor | DefaultAccessory) => None
      case ItemSlot(x) => Some(x)
    }
    (toSet, defaultsExcluded) match {
      case (ItemSlot(iv), Some(item)) => ItemSlot(item).set(iv) match {
        case Some((newValue, old)) => (EquipmentSet(items.updated(idx, newValue)), old)
        case None => (this, toSet)
      }
      case (s@ItemSlot(iv), None) => (EquipmentSet(items.updated(idx, s)), EmptySlot)
      case (s@EmptySlot, Some(item)) => (EquipmentSet(items.updated(idx, s)), ItemSlot(item))
      case _ => (this, EmptySlot)
    }
  }

  /**
    * This method returns index of the most "weak" compatible Equippable. The definition of "weak" is a subject to change btw
    */
  def optimalIndexFor(v: Equippable)(implicit ev: ItemMerger[Equippable, Equippable]): Int = {
    val compatible = items.filter(x => ev.asCompatible(x.get, v).nonEmpty)
    compatible.indexOf(ItemSlot(DefaultWeapon)) match {
      case -1 => items.indexOf(compatible.head)
      case idx => idx
    }
  }
}

object EquipmentSet {
  private def extractValue[T <: Equippable](v: Vector[Slot[Equippable]], idx: Int)
                                           (implicit ev: DefaultValue[T], ev2: SubTyper[Equippable, T]): T = getEquip[T](v, idx) match {
    case Some(i) => i
    case _ => ev.self
  }

  private def getEquip[T <: Equippable](v: Vector[Slot[Equippable]], idx: Int)
                                       (implicit matcher: SubTyper[Equippable, T]): Option[T] = {
    if (idx < v.length && !v(idx).isEmpty) {
      matcher.asSub(v(idx).get) match {
        case a@Some(x) => a
        case _ => None
      }
    } else None
  }
}
