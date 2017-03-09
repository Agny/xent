package ru.agny.xent.battle.core

import ru.agny.xent.battle.unit.{Soul, Troop}

trait Tactic {
  def execute(target: Troop): (Soul, Troop)
}

case class BasicTactic(u: Soul) extends Tactic {
  def execute(target: Troop): (Soul, Troop) = {
    val weapon = u.equip.weapons.maxBy(_.damage)
    val attackBy = u.attackPotential(weapon).strongestRaw
    val cast = weapon.damage.cast
    val damage = OutcomeDamage(attackBy, cast)
    val res = target.receiveDamage(damage)
    //TODO fightback
    (u, res)
  }
}

object Tactic {
  def get(u: Soul): Tactic = {
    //TODO choose tactic
    BasicTactic(u)
  }
}