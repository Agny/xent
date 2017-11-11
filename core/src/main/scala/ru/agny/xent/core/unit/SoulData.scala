package ru.agny.xent.core.unit

import ru.agny.xent.battle.unit.Potential
import ru.agny.xent.core.LifePowered
import ru.agny.xent.core.unit.Stats.WeaponRate
import ru.agny.xent.core.unit.equip.{Defensive, Equipment}
import ru.agny.xent.core.utils.UserType.ItemWeight

//TODO Equipment boosting stats
/** @param level is required for equipping items and learning skills. Represents experience, accumulated by the soul in this incarnation */
case class SoulData(level: Level, spiritPower: Int, private val stats: Stats, private val skills: Vector[Skill]) extends LifePowered {

  def armor(implicit equipment: Equipment): Int = stats.effectiveArmor(equipment)

  def spirit(implicit equipment: Equipment): Spirit = stats.effectiveSpirit(equipment, spiritPower)

  def speed(implicit equipment: Equipment): Int = stats.effectiveSpeed(equipment)

  def weight(implicit equipment: Equipment): Int = equipment.weight + stats.weight

  def endurance(implicit equipment: Equipment): Int = stats.effectiveEndurance(equipment)

  def initiative(implicit equipment: Equipment): Int = stats.effectiveInitiative(equipment)

  def attackModifiers(implicit equipment: Equipment): Vector[WeaponRate] = stats.attackModifiers(equipment)

  def defenseModifiers(implicit equipment: Equipment): Potential = Potential(equipment.props()(Defensive))

  def receiveDamage(dmg: Int)(implicit equipment: Equipment): SoulData = {
    val updatedSpirit = spirit.change(-dmg)
    val expEarned = {
      if (updatedSpirit >= 0) dmg
      else spirit.points
    }
    copy(level = level.gainExp(expEarned), spiritPower = spirit.change(-dmg))
  }

  def gainExp(amount: Int) = copy(level = level.gainExp(amount))

  def carryPower(implicit equipment: Equipment): ItemWeight = stats.carryPower(equipment)

  override def toLifePower = stats.weight
}
