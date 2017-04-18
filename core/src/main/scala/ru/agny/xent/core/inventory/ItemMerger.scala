package ru.agny.xent.core.inventory

import ru.agny.xent.battle.unit.inventory.{Accessory, Armor, Weapon, Equippable}
import ru.agny.xent.core.{ResourceUnit, Item}

trait ItemMerger[-From <: Item, To <: Item] {
  def asCompatible(a: From, b: To): Option[To]
}

object ItemMerger {
  object implicits {
    implicit object ResourceMerger extends ItemMerger[Item, Item] {
      override def asCompatible(a: Item, b: Item): Option[ResourceUnit] = (a, b) match {
        case (current: ResourceUnit, toSet: ResourceUnit) => Some(current.add(toSet))
        case _ => None
      }
    }
    implicit object EquippableMerger extends ItemMerger[Equippable, Equippable] {
      override def asCompatible(a: Equippable, b: Equippable): Option[Equippable] = (a, b) match {
        case (current: Weapon, toSet: Weapon) => Some(toSet)
        case (current: Armor, toSet: Armor) => Some(toSet)
        case (current: Accessory, toSet: Accessory) => Some(toSet)
        case _ => None
      }
    }
  }
}
