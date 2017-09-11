package ru.agny.xent.battle

import ru.agny.xent.core.MapObject
import ru.agny.xent.core.unit.Soul
import ru.agny.xent.core.unit.equip.OutcomeDamage

trait Tactic {
  val self: Soul

  def execute(enemy: MapObject): (Soul, MapObject) = {
    if (enemy.isActive) {
      val damage = damageOutcome(enemy)
      val expEarned = damage.calc().toInt
      val target = chooseTarget(enemy)
      val res = enemy.receiveDamage(damage, target.map(_.id))
      //TODO fightback
      (self.gainExp(expEarned), res)
    } else {
      (self, enemy)
    }
  }

  def damageOutcome(enemy: MapObject): OutcomeDamage

  def chooseTarget(enemy: MapObject): Vector[Targetable]
}

case class BasicTactic(self: Soul) extends Tactic {

  def damageOutcome(enemy: MapObject) = {
    implicit val target = enemy
    val (wpn, rates) = self.attackRates.head
    val (attackBy, bonus) = rates.maxBy(_._1.value)
    val cast = wpn.damage.cast
    OutcomeDamage(attackBy, cast + bonus)
  }

  def chooseTarget(enemy: MapObject): Vector[Targetable] = Vector(enemy.body.head)
}

object Tactic {
  def get(u: Soul): Tactic = {
    //TODO choose tactic
    BasicTactic(u)
  }
}