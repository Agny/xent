package ru.agny.xent.battle

import ru.agny.xent.UserType._
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.unit.Occupation
import ru.agny.xent.core.utils.NESeq

case class Combatants(troops: NESeq[(Troop, Occupation)], queue: Vector[(Troop, Occupation)]) {

  def queue(t: Vector[(Troop, Occupation)]): Combatants = copy(queue = queue ++: t)

  def groupByUsers: Map[UserId, Map[ObjectId, Troop]] = {
    val empty = Map.empty[UserId, Map[ObjectId, Troop]].withDefaultValue(Map.empty)
    troops.foldLeft(empty)((m, t) => m.updated(t._1.user, m(t._1.user).updated(t._1.id, t._1)))
  }

  def free: Vector[(Troop, Occupation)] = (troops ++ queue).toVector
}

object Combatants {
  type TO = (Troop, Occupation)
  type Pool = Map[UserId, Map[ObjectId, Troop]]

  def adjustPool(pool: Pool, value: Troop): Pool = pool.updated(value.user, pool(value.user).updated(value.id, value))

  def isBattleNeeded(troops: Seq[Troop]) = troops.filter(_.isAbleToFight).map(_.user).distinct.length > 1

  def prepareToNextRound(self: Combatants, afterBattle: Vector[Troop]): (Option[Combatants], Vector[TO]) = {
    val withOccupation = self.troops.map(x => x._1.id -> x._2).toMap
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
