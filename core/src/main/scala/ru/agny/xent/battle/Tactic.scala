package ru.agny.xent.battle

import ru.agny.xent.core.MapObject
import ru.agny.xent.core.unit.Soul
import ru.agny.xent.core.unit.equip.OutcomeDamage

trait Tactic {
  val self: Soul

  def execute[Target <: MapObject](enemy: Target): (Soul, Target) = {
    if (enemy.isActive) {
      val damage = damageOutcome(enemy)
      val expEarned = damage.calc().toInt
      val target = chooseTarget(enemy)
      val res = enemy.receiveDamage(damage, target.map(_.id))
      //TODO fightback
      (self.gainExp(expEarned), res.asInstanceOf[Target])
    } else {
      (self, enemy)
    }
  }

  def damageOutcome[Target <: MapObject](enemy: Target): OutcomeDamage

  def chooseTarget[Target <: MapObject](enemy: Target): Vector[Targetable]
}

case class BasicTactic(self: Soul) extends Tactic {

  def damageOutcome[Target <: MapObject](enemy: Target) = {
    implicit val target = enemy
    val (wpn, rates) = self.attackRates.head
    val (attackBy, bonus) = rates.maxBy(_._1.value)
    val cast = wpn.damage.cast
    OutcomeDamage(attackBy, cast + bonus)
  }

  def chooseTarget[Target <: MapObject](enemy: Target): Vector[Targetable] = Vector(enemy.body.head)
}

object Tactic {
  def get(u: Soul): Tactic = {
    //TODO choose tactic
    BasicTactic(u)
  }
}