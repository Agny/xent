package ru.agny.xent.battle

import ru.agny.xent.UserType._
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.unit.Occupation
import ru.agny.xent.core.utils.NESeq

case class Combatants(troops: NESeq[Troop], queue: Vector[Troop]) {

  def queue(t: Vector[Troop]): Combatants = copy(queue = queue ++: t)

  def groupByUsers: Map[UserId, Map[ObjectId, Troop]] = {
    val empty = Map.empty[UserId, Map[ObjectId, Troop]].withDefaultValue(Map.empty)
    troops.foldLeft(empty)((m, t) => m.updated(t.user, m(t.user).updated(t.id, t)))
  }

  def free: Vector[Troop] = (troops ++ queue).toVector
}

object Combatants {
  type Pool = Map[UserId, Map[ObjectId, Troop]]

  def adjustPool(pool: Pool, value: Troop): Pool = pool.updated(value.user, pool(value.user).updated(value.id, value))

  def isBattleNeeded(troops: Seq[Troop]) = troops.filter(_.isAbleToFight).map(_.user).distinct.length > 1

  def prepareToNextRound(self: Combatants, afterBattle: Vector[Troop]): (Option[Combatants], Vector[Troop]) = {
    val withOccupation = self.troops.map(x => x.id -> x).toMap
    val (fallen, alive) = sendFallenToHome(afterBattle, withOccupation)
    val (exhausted, fresh) = freeExhaustedFromBattle(alive, withOccupation)
    val out = fallen ++ exhausted
    val ready = resumePreviousOccupation(fresh, withOccupation) ++ self.queue
    if (isBattleNeeded(ready.unzip._1)) (Some(Combatants(NESeq(ready), Vector.empty)), out)
    else (None, out ++ ready)
  }

  //TODO calculate home coordinates for fallen
  private def sendFallenToHome(troops: Vector[Troop], occupations: Map[ObjectId, Occupation]) = {
    val (alive, fallen) = troops.partition(_.isActive)
    (fallen.map(x => x -> occupations(x.id)), alive)
  }

  private def freeExhaustedFromBattle(troops: Vector[Troop], occupations: Map[ObjectId, Occupation]) = {
    val (fresh, exhausted) = troops.partition(_.endurance > 0)
    (resumePreviousOccupation(exhausted, occupations), fresh)
  }

  private def resumePreviousOccupation(troops: Vector[Troop], occupations: Map[ObjectId, Occupation]) = troops.map(x => x -> occupations(x.id))
}
