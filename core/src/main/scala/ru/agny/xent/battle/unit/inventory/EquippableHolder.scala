package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.core.inventory._

case class EquippableHolder(slots: Vector[Slot[Equippable]]) extends SlotHolder[Equippable] {

  import ItemSubChecker.implicits._
  import EquippableHolder._
  import DefaultValue.implicits._

  val mainHand = extractValue[Weapon](slots)
  val offHand = extractValue[Weapon](slots.diff(Vector(ItemSlot(mainHand))))
  val armor = extractValue[Armor](slots)
  val accessory = extractValue[Accessory](slots)

  val items = Vector(mainHand, offHand, armor, accessory)
  val activeItems = Vector(mainHand, offHand)
  val passiveItems = Vector(armor, accessory)

  def set(idx: Int, v: Slot[Equippable])
         (implicit ev: ItemMerger[Equippable, Equippable]): (EquippableHolder, Slot[Equippable]) = {
    val (updated: Slot[Equippable], replaced: Slot[Equippable]) = idx match {
      case 0 => v match {
        case i@ItemSlot(iv) => ItemSlot(mainHand).set(iv) match {
          case Some((nv, rpl)) => (nv, rpl)
          case None => (mainHand, v)
        }
        case EmptySlot => (DefaultWeapon, mainHand)
      }
      case 1 => v match {
        case i@ItemSlot(iv) => ItemSlot(offHand).set(iv) match {
          case Some((nv, rpl)) => (nv, rpl)
          case None => (offHand, v)
        }
        case EmptySlot => (DefaultWeapon, offHand)
      }
      case 2 => v match {
        case i@ItemSlot(iv) => ItemSlot(armor).set(iv) match {
          case Some((nv, rpl)) => (nv, rpl)
          case None => (armor, v)
        }
        case EmptySlot => (DefaultWeapon, armor)
      }
      case 3 => v match {
        case i@ItemSlot(iv) => ItemSlot(accessory).set(iv) match {
          case Some((nv, rpl)) => (nv, rpl)
          case None => (accessory, v)
        }
        case EmptySlot => (DefaultWeapon, accessory)
      }
      case _ => None
    }
    val replacedIdx = slots.indexOf(replaced)
    val updateSlots = if (replacedIdx == -1)
      slots
    else {
      slots.updated(replacedIdx, updated)
    }
    (EquippableHolder(updateSlots), replaced)
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

}
