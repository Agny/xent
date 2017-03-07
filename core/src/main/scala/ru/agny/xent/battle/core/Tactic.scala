package ru.agny.xent.battle.core

import ru.agny.xent.battle.unit.{Soul, Troop}

trait Tactic {
  def execute(target: Troop): (Soul, Troop)
}

case class BasicTactic(u: Soul) extends Tactic {
  def execute(target: Troop): (Soul, Troop) = {
    val attackBy = u.attackPotential.strongestRaw
    val res = target.receiveDamage(Damage(attackBy.attr, attackBy.value))
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