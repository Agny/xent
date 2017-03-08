package ru.agny.xent.battle.unit.inventory

import ru.agny.xent.battle.core._

case class Equipment(private val mainHand: Weapon = DefaultWeapon,
                     private val offHand: Weapon = DefaultWeapon,
                     private val armor: Armor = DefaultArmor,
                     private val accessory: Accessory = DefaultAccessory) {

  def props(implicit mode: Mode): Seq[Property] =
    Seq(mainHand, offHand, armor, accessory).foldLeft(Map.empty[Attribute, Int])((a, b) =>
      b.attrs.foldLeft(a)(collectPotential)
    ).map(x => Property(x._1, x._2, mode)).toSeq

  def weapons = Seq(mainHand, offHand)

  private def collectPotential(attrs: Map[Attribute, Int], prop: Property)(implicit mode: Mode) = prop.mode match {
    case correct if correct == mode =>
      if (attrs.contains(prop.attr)) {
        attrs + (prop.attr -> (attrs(prop.attr) + prop.value))
      } else {
        attrs + (prop.attr -> prop.value)
      }
    case _ => attrs
  }
}
object Equipment {
  def empty(): Equipment = Equipment()
}
