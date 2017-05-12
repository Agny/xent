package ru.agny.xent.core.inventory

import ru.agny.xent.battle.unit.inventory.{Accessory, Armor, Equippable, Weapon}
import ru.agny.xent.core.Item
import ru.agny.xent.core.utils.SubTyper

object ItemSubTyper {
  object implicits {
    implicit object EquippableMatcher extends SubTyper[Item, Equippable] {
      override def asSub(a: Item): Option[Equippable] = a match {
        case x: Equippable => Some(x)
        case _ => None
      }
    }
    implicit object WeaponMatcher extends SubTyper[Equippable, Weapon] {
      override def asSub(a: Equippable): Option[Weapon] = a match {
        case x: Weapon => Some(x)
        case _ => None
      }
    }
    implicit object ArmorMatcher extends SubTyper[Equippable, Armor] {
      override def asSub(a: Equippable): Option[Armor] = a match {
        case x: Armor => Some(x)
        case _ => None
      }
    }
    implicit object AccessoryMatcher extends SubTyper[Equippable, Accessory] {
      override def asSub(a: Equippable): Option[Accessory] = a match {
        case x: Accessory => Some(x)
        case _ => None
      }
    }
  }
}
