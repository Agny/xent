package ru.agny.xent.battle.core

import ru.agny.xent.battle.unit.{Soul, Troop}

trait Tactic {
  val self: Soul

  def execute(enemy: Troop): (Soul, Troop) = {
    val damage = damageOutcome(enemy)
    val target = chooseTarget(enemy)
    val res = enemy.receiveDamage(damage, target.map(_.id))
    //TODO fightback
    (self, res)
  }

  def damageOutcome(enemy: Troop): OutcomeDamage

  def chooseTarget(enemy: Troop): Seq[Soul]
}

case class BasicTactic(self: Soul) extends Tactic {

  def damageOutcome(enemy: Troop) = {
    val weapon = self.equip.weapons.maxBy(_.damage)
    val attackBy = self.attackPotential(weapon).strongestRaw
    val cast = weapon.damage.cast
    OutcomeDamage(attackBy, cast)
  }

  def chooseTarget(enemy: Troop): Seq[Soul] = Seq(enemy.units.head)
}

object Tactic {
  def get(u: Soul): Tactic = {
    //TODO choose tactic
    BasicTactic(u)
  }
}