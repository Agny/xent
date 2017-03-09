package ru.agny.xent.battle.core

import ru.agny.xent.battle.unit.Potential

trait Damage[T] {
  val attr: Attribute

  def calc(): T
}

case class IncomeDamage(attr: Attribute, defensePotential: Potential, armor: Int, damage: Double) extends Damage[Int] {
  def calc(): Int = (damage * ((100 - defensePotential.to(attr)) / 100)).round.toInt - armor
}
case class OutcomeDamage(attribute: Property, baseDamage: Int) extends Damage[Double] {
  val attr = attribute.attr

  def calc(): Double = baseDamage * ((100d + attribute.value) / 100)
}
