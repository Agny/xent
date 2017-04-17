package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.core.inventory.{SlotHolder, ItemSlot, Slot, ItemSubChecker}

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
}

object EquippableHolder {
  private def extractValue[T <: Equippable](v: Vector[Slot[Equippable]])
                                           (implicit ev: DefaultValue[T], ev2: ItemSubChecker[Equippable, T]): T = getEquip[T](v) match {
    case Vector(a) => a
    case _ => ev.self
  }

  private def getEquip[T <: Equippable](v: Vector[Slot[Equippable]])
                                       (implicit matcher: ItemSubChecker[Equippable, T]): Vector[T] =
    v.filter(s => !s.isEmpty).flatMap(s => matcher.asSub(s.get) match {
      case a@Some(x) => a
      case _ => None
    })

}
