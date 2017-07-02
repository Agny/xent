package ru.agny.xent.core.inventory

import ru.agny.xent.core.unit.equip.{Weapon, Equippable, Armor, Accessory}
import ru.agny.xent.core.{ItemStack, Item}

trait ItemMerger[-From <: Item, To <: Item] {
  def asCompatible(a: From, b: To): Option[To]
}

object ItemMerger {
  object implicits {
    implicit object ResourceMerger extends ItemMerger[Item, Item] {
      override def asCompatible(a: Item, b: Item): Option[ItemStack] = (a, b) match {
        case (current: ItemStack, toSet: ItemStack) => Some(current.add(toSet))
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
