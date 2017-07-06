package ru.agny.xent.core.unit

import ru.agny.xent.core.unit.characteristic.{Agility, PresencePower, Strength}
import ru.agny.xent.core.unit.equip.{Equipment, StatProperty}

//TODO game balancing
case class Stats(private val s: Vector[StatProperty]) {
  /**
    * Effective armor value defined as
    * math.floor(equipment_armor*(1 + strength * 0.2 + presence * 0.12 + agility * 0.07))
    */
  def effectiveArmor(eq: Equipment): Int = {
    val armor = eq.armor.value //TODO Armor class, e.g. heavy/light?
    val props = s.filter(x => x.prop == Strength || x.prop == PresencePower || x.prop == Agility)
    val modifier = props.map {
      case StatProperty(Strength, lvl) => lvl.value * 0.2
      case StatProperty(PresencePower, lvl) => lvl.value * 0.12
      case StatProperty(Agility, lvl) => lvl.value * 0.07
      case _ => 0d
    }.sum
    math.floor(armor * (1 + modifier)).toInt
  }

  /**
    * Effective spirit value defined as
    * capacity = spirit_capacity + presence * 2
    * regen = spirit_regen + math.floor(presence * 0.1 + strength * 0.05)
    */
  def effectiveSpirit(eq: Equipment, spirit: Spirit): Spirit = {
    val regen = spirit.regen
    val capacity = spirit.capacity
    val props = s.filter(x => x.prop == Strength || x.prop == PresencePower)
    val (regenBonus, capacityBonus) = props.map {
      case StatProperty(PresencePower, lvl) => (lvl.value * 0.1, lvl.value * 2)
      case StatProperty(Strength, lvl) => (lvl.value * 0.05, 0)
      case _ => (0d, 0)
    }.unzip
    Spirit(spirit.points, regen + math.floor(regenBonus.sum).toInt, capacity + capacityBonus.sum)
  }

  def effectiveSpeed(eq: Equipment): Int = s.find(_.prop == Agility) match {
    case Some(agility) => Speed.default + math.floor(agility.level.value * 0.5).toInt
    case _ => Speed.default
  }

  def weight: Int = s.map(_.toLifePower).sum
}

object Stats {
  val default: Stats = Stats(Vector.empty)
}
