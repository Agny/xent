package ru.agny.xent.battle

import ru.agny.xent.UserType._
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.MapObject
import ru.agny.xent.core.utils.NESeq

case class Combatants(troops: NESeq[MapObject], queue: Vector[Troop]) {

  def queue(t: Vector[Troop]): Combatants = copy(queue = queue ++: t)

  def groupByUsers: Map[UserId, Map[ObjectId, MapObject]] = {
    val empty = Map.empty[UserId, Map[ObjectId, MapObject]].withDefaultValue(Map.empty)
    troops.foldLeft(empty)((m, t) => m.updated(t.user, m(t.user).updated(t.id, t)))
  }

  def free: Vector[MapObject] = (troops ++ queue).toVector
}

object Combatants {
  type Pool = Map[UserId, Map[ObjectId, MapObject]]

  def adjustPool(pool: Pool, value: MapObject): Pool = pool.updated(value.user, pool(value.user).updated(value.id, value))

  def isBattleNeeded(troops: Seq[MapObject]): Boolean = troops.map(_.user).distinct.length > 1

  def prepareToNextRound(self: Combatants, afterBattle: Vector[MapObject]): (Option[Combatants], Vector[MapObject]) = {
    val (alive, out) = getAbleToFight(afterBattle)
    val ready = alive ++ self.queue
    if (isBattleNeeded(ready)) (Some(Combatants(NESeq(ready), Vector.empty)), out)
    else (None, out ++ ready)
  }

  private def getAbleToFight(troops: Vector[MapObject]) = troops.partition(_.isAbleToFight)
}
