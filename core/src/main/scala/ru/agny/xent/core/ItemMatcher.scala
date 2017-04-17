package ru.agny.xent.core

import ru.agny.xent.battle.unit.inventory.{Accessory, Armor, Weapon, Equippable}

trait ItemMatcher[-From <: Item, To <: Item] {
  def toStack(a: From, b: To): Option[To]
}

object ItemMatcher {
  object implicits {
    implicit object ResourceMatcher extends ItemMatcher[Item, Item] {
      override def toStack(a: Item, b: Item): Option[ResourceUnit] = (a, b) match {
        case (toSet: ResourceUnit, current: ResourceUnit) => Some(current.add(toSet))
        case _ => None
      }
    }

    implicit object ExtractableMatcher extends ItemMatcher[Extractable, Extractable] {
      override def toStack(a: Extractable, b: Extractable): Option[Extractable] = None
    }
  }
}

trait ItemMatcher2[-From <: Item, To <: Item] {
  def toStack(a: From): Option[To]
}

object ItemMatcher2 {
  object implicits {
    implicit object WeaponMatcher extends ItemMatcher2[Equippable, Weapon] {
      override def toStack(a: Equippable): Option[Weapon] = a match {
        case x: Weapon => Some(x)
        case _ => None

      }
    }
    implicit object ArmorMatcher extends ItemMatcher2[Equippable, Armor] {
      override def toStack(a: Equippable): Option[Armor] = a match {
        case x: Armor => Some(x)
        case _ => None

      }
    }
    implicit object AccessoryMatcher extends ItemMatcher2[Equippable, Accessory] {
      override def toStack(a: Equippable): Option[Accessory] = a match {
        case x: Accessory => Some(x)
        case _ => None

      }
    }
  }
}
