package ru.agny.xent.core.inventory

import ru.agny.xent.core.unit.equip.{Accessory, Armor, Equippable, Weapon}
import ru.agny.xent.core.utils.SubTyper

object ItemSubTyper {
  object implicits {
    implicit object EquippableMatcher extends SubTyper[Item, Equippable]
    implicit object WeaponMatcher extends SubTyper[Equippable, Weapon]
    implicit object ArmorMatcher extends SubTyper[Equippable, Armor]
    implicit object AccessoryMatcher extends SubTyper[Equippable, Accessory]
  }
}
