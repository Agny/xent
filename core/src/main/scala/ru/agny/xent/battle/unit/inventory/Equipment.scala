package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core._

case class Equipment(private val mainHand: Weapon = DefaultWeapon,
                     private val offHand: Weapon = DefaultWeapon,
                     val armor: Armor = DefaultArmor,
                     private val accessory: Accessory = DefaultAccessory) {

  def props(wpn: Weapon = DefaultWeapon)(implicit mode: Mode): Seq[Property] =
    (mode match {
      case Defensive =>
        Seq(mainHand, offHand, armor, accessory).foldLeft(Map.empty[Attribute, Int])((a, b) =>
          b.attrs.foldLeft(a)(collectAllPotential)
        )
      case Offensive =>
        val wpnAttrs = wpn.attrs.map(x => x.attr -> x.value).toMap
        Seq(armor, accessory).foldLeft(wpnAttrs)((a, b) =>
          b.attrs.foldLeft(a)(collectSpecifiedPotential)
        )
    }).map{case (attr, power) => Property(attr, power, mode)}.toSeq

  def weapons = Seq(mainHand, offHand)

  private def collectAllPotential(attrs: Map[Attribute, Int], prop: Property)(implicit mode: Mode) = prop.mode match {
    case correct if correct == mode =>
      if (attrs.contains(prop.attr)) {
        attrs + (prop.attr -> (attrs(prop.attr) + prop.value))
      } else {
        attrs + (prop.attr -> prop.value)
      }
    case _ => attrs
  }

  private def collectSpecifiedPotential(attrs: Map[Attribute, Int], prop: Property)(implicit mode: Mode) = prop.mode match {
    case correct if correct == mode && attrs.contains(prop.attr) =>
      attrs + (prop.attr -> (attrs(prop.attr) + prop.value))
    case _ => attrs
  }
}
object Equipment {
  def empty(): Equipment = Equipment()
}
