package ru.agny.xent.core.unit

import ru.agny.xent.battle.unit.Potential
import ru.agny.xent.core.unit.equip.{Weapon, AttrProperty, Dice, Equipment}

//TODO Equipment boosting stats
case class SoulData(level: Level, private val spiritPower: Spirit, private val stats: Stats, private val skills: Vector[Skill]) {

  import SoulData._

  def armor(implicit equipment: Equipment): Int = stats.effectiveArmor(equipment)

  def spirit(implicit equipment: Equipment): Spirit = stats.effectiveSpirit(equipment, spiritPower)

  def speed(implicit equipment: Equipment): Int = stats.effectiveSpeed(equipment)

  def weight(implicit equipment: Equipment): Int = equipment.weight + stats.weight

  def endurance(implicit equipment: Equipment): Int = ???

  def initiative(implicit equipment: Equipment): Int = ???

  def attackModifiers(implicit equip: Equipment): (Weapon, PotentialDetailed) = ???

  def defenseModifiers(implicit equip: Equipment): Potential = ???

  def speedModifiers(implicit equip: Equipment) = ???

  def spiritModifiers(implicit equip: Equipment) = ???

  def receiveDamage(dmg: Int)(implicit equip: Equipment): SoulData = copy(spiritPower = spirit.change(dmg))

}

object SoulData {
  type BonusDamage = Dice
  type PotentialDetailed = Vector[(AttrProperty, BonusDamage)]
}
