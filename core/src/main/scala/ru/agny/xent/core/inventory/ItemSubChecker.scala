package ru.agny.xent.core.inventory

import ru.agny.xent.battle.unit.inventory.{Accessory, Armor, Equippable, Weapon}
import ru.agny.xent.core.Item

trait ItemSubChecker[From <: Item, To <: Item] {
  def asSub(a: From): Option[To]
}

object ItemSubChecker {
  object implicits {
    implicit object EquippableMatcher extends ItemSubChecker[Item, Equippable] {
      override def asSub(a: Item): Option[Equippable] = a match {
        case x: Equippable => Some(x)
        case _ => None
      }
    }
    implicit object WeaponMatcher extends ItemSubChecker[Equippable, Weapon] {
      override def asSub(a: Equippable): Option[Weapon] = a match {
        case x: Weapon => Some(x)
        case _ => None
      }
    }
    implicit object ArmorMatcher extends ItemSubChecker[Equippable, Armor] {
      override def asSub(a: Equippable): Option[Armor] = a match {
        case x: Armor => Some(x)
        case _ => None
      }
    }
    implicit object AccessoryMatcher extends ItemSubChecker[Equippable, Accessory] {
      override def asSub(a: Equippable): Option[Accessory] = a match {
        case x: Accessory => Some(x)
        case _ => None
      }
    }
  }
}
