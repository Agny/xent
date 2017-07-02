package ru.agny.xent.core.unit.equip

import ru.agny.xent.battle.unit.Potential

trait Damage[T] {
  val attr: Attribute

  def calc(): T
}

/**
  * Damage to receive by soul is calculated as
  * OutcomeDamage * EffectiveDefense - armor
  *
  */
case class IncomeDamage(attr: Attribute, defensePotential: Potential, armor: Int, damage: Double) extends Damage[Int] {
  def calc(): Int = (damage * ((100 - defensePotential.to(attr)) / 100)).round.toInt - armor
}

/**
  * Damage to inflict by soul is calculated as
  * DiceCast * EffectiveAttack
  *
  */
case class OutcomeDamage(attribute: Property, baseDamage: Int) extends Damage[Double] {
  val attr = attribute.attr

  def calc(): Double = baseDamage * ((100d + attribute.value) / 100)
}
