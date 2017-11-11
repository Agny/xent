package ru.agny.xent.core.unit

import ru.agny.xent.core.unit.characteristic._
import ru.agny.xent.core.unit.equip._
import ru.agny.xent.core.utils.UserType.ItemWeight

//TODO game balancing
case class Stats(private val s: Vector[StatProperty], private val base: SpiritBase) {

  import Stats._

  /**
    * Effective armor value defined as
    * math.floor(equipment_armor + strength * 0.2 + presence * 0.12 + agility * 0.07)
    */
  def effectiveArmor(eq: Equipment): Int = {
    val armor = eq.armor.value //TODO Armor class, e.g. heavy/light?
    val strength = getValueOrZero(Strength, s)
    val agility = getValueOrZero(Agility, s)
    val presence = getValueOrZero(PresencePower, s)
    val modifier = strength * 0.2 + presence * 0.12 + agility * 0.07
    math.floor(armor + modifier).toInt
  }

  /**
    * Effective spirit value defined as
    * capacity = spirit_capacity + presence * 2
    * regen = spirit_regen + math.floor(presence * 0.1 + strength * 0.05)
    */
  def effectiveSpirit(eq: Equipment, current: Int): Spirit = {
    val regen = base.regen
    val capacity = base.capacity
    val props = s.filter(x => x.prop == Strength || x.prop == PresencePower)
    val (regenBonus, capacityBonus) = props.map {
      case StatProperty(PresencePower, lvl) => (lvl.value * 0.1, lvl.value * 2)
      case StatProperty(Strength, lvl) => (lvl.value * 0.05, 0)
      case _ => (0d, 0)
    }.unzip
    Spirit(current, SpiritBase(regen + math.floor(regenBonus.sum).toInt, capacity + capacityBonus.sum))
  }

  /**
    * Effective speed value defined as
    * speed_default + math.floor(agility * 0.5))
    */
  def effectiveSpeed(eq: Equipment): Int = s.find(_.prop == Agility) match {
    case Some(agility) => Speed.default + math.floor(agility.level.value * 0.5).toInt
    case _ => Speed.default
  }

  def weight: Int = s.map(_.toLifePower).sum

  /**
    * Effective endurance value defined as
    * endurance_default + math.floor(presence * 0.2))
    */
  def effectiveEndurance(eq: Equipment): Int = s.find(_.prop == PresencePower) match {
    case Some(ppower) => Endurance.default + math.floor(ppower.level.value * 0.2).toInt
    case _ => Endurance.default
  }

  /**
    * Effective initiative value defined as
    * math.floor(base_initiative + agility * 0.2 - presence * 0.1 - intelligence * 0.1)
    */
  def effectiveInitiative(eq: Equipment): Int = {
    val initiative = getValueOrZero(Initiative, s)
    val agility = getValueOrZero(Agility, s)
    val presence = getValueOrZero(PresencePower, s)
    val intelligence = getValueOrZero(Intelligence, s)
    math.floor(initiative + agility * 0.2 - presence * 0.1 - intelligence * 0.1).toInt
  }

  /**
    * Attack modifiers are bonus dice casts, which value depends on the weapon type attack and corresponding Characteristic
    * bonus_value = sum(Dice(characteristic_level_in_tier, level_tier) for each level tier that characteristic got) * characteristic_modifier
    *
    */
  def attackModifiers(eq: Equipment): Vector[WeaponRate] = {
    val mh = eq.holder.set.mainHand
    val oh = eq.holder.set.offHand
    val mhAttrs = eq.props(mh)(Offensive)
    val ohAttrs = eq.props(oh)(Offensive)
    Vector((mh, attributesWithBonus(mhAttrs, s)), (oh, attributesWithBonus(ohAttrs, s)))
  }

  def carryPower(eq: Equipment): ItemWeight = {
    val strength = getValueOrZero(Strength, s)
    val presence = getValueOrZero(PresencePower, s)
    strength * 20 + presence * 10
  }

}

object Stats {
  type BonusDamage = Int
  type WeaponRate = (Weapon, Vector[(AttrProperty, BonusDamage)])
  val defaultSpiritBase = SpiritBase(0, 0)
  val default: Stats = Stats(Vector.empty, defaultSpiritBase)

  def apply(stats: Vector[StatProperty]): Stats = Stats(stats, defaultSpiritBase)

  private def getValueOrZero(prop: Characteristic, from: Vector[StatProperty]) = from.find(_.prop == prop).map(_.level.value) getOrElse 0

  private def attributesWithBonus(attrs: Vector[AttrProperty], stats: Vector[StatProperty]): Vector[(AttrProperty, BonusDamage)] = {
    attrs.map(a => a -> stats.map(_.bonusDamage(a.prop)).sum)
  }
}
