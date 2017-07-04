package ru.agny.xent.core.unit

import ru.agny.xent.battle.unit.Potential
import ru.agny.xent.core.unit.equip.{Weapon, Property, Dice, Equipment}

case class SoulData(level: Level, private val spiritPower: Spirit, private val stats: Stats, private val skills: Vector[Skill]) {

  import SoulData._

  def armor(implicit equipment: Equipment): Int = ???

  def spirit(implicit equipment: Equipment): Int = ???

  def speed(implicit equipment: Equipment): Int = ???

  def weight(implicit equipment: Equipment): Int = ???

  def endurance(implicit equipment: Equipment): Int = ???

  def initiative(implicit equipment: Equipment): Int = ???

  def attackModifiers(implicit equip: Equipment): (Weapon, PotentialDetailed) = ???

  def defenseModifiers(implicit equip: Equipment): Potential = ???

  def speedModifiers(implicit equip: Equipment) = ???

  def spiritModifiers(implicit equip: Equipment) = ???

  def receiveDamage(dmg: Int): SoulData = copy(spiritPower = spiritPower.change(dmg))

}

object SoulData {
  type BonusDamage = Dice
  type PotentialDetailed = Vector[(Property, BonusDamage)]
}
