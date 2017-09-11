package ru.agny.xent.battle

import ru.agny.xent.UserType.UserId
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.{Coordinate, MapObject}
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.utils.NESeq

import scala.annotation.tailrec
import scala.util.Random

case class Battle(pos: Coordinate, private val combatants: Combatants, round: Round) extends Event {

  import Battle._
  import Combatants._

  def tick(progress: ProgressTime): (Option[Battle], Vector[MapObject], ProgressTime) = rec_tick(progress, Some(this), Vector.empty)

  def addTroops(t: Vector[Troop]): Battle = copy(combatants = combatants.queue(t))

  //TODO unite troops to Squads
  private def toNextRound: (Option[Battle], Vector[MapObject]) = {
    val troopsByUser = combatants.groupByUsers
    val (troopsResult, _) = nextAttack(troopsByUser.values.flatten.unzip._2.collect { case t: Troop => t }.toVector, troopsByUser)
    val (next, out) = prepareToNextRound(combatants, troopsResult)
    next match {
      case Some(v) =>
        val r = round.next(NESeq(v.troops))
        (Some(Battle(pos, v, r)), out)
      case None => (None, out)
    }
  }

  @tailrec private def nextAttack(attackers: Vector[Troop], pool: Pool): (Vector[MapObject], Pool) = {
    val (mostInitiative +: tail) = attackers.sortBy(-_.initiative)
    val target = claimEnemy(mostInitiative.user, pool.map(x => x._1 -> x._2.values))
    val (attacker, attacked) = attack(mostInitiative, target)
    val poolWithAttacker = Combatants.adjustPool(pool, attacker)
    val updPool = Combatants.adjustPool(poolWithAttacker, attacked)

    val nextAttackers = validateAttackers(tail, attacked)
    if (isNextAttackAvailable(nextAttackers, updPool)) nextAttack(nextAttackers, updPool)
    else (updPool.values.flatten.unzip._2.toVector, updPool)
  }

  private def validateAttackers(base: Vector[Troop], attacked: MapObject): Vector[Troop] = {
    val others = base.filterNot(_.id == attacked.id)
    if (others.size == base.size) others
    else attacked match {
      case t: Troop if t.isAbleToFight => t +: others
      case _ => others
    }
  }

  private def isNextAttackAvailable(attackers: Vector[Troop], pool: Pool) = {
    if (attackers.isEmpty) false
    else {
      val possibleTargets = attackers.flatMap(x => pool.filter(_._1 != x.user).values.flatten.unzip._2)
      possibleTargets.map(_.isActive).nonEmpty
    }
  }

  private def claimEnemy(self: UserId, troops: Map[UserId, Iterable[MapObject]]): MapObject = {
    val mbtargets = troops.filter(_._1 != self).values.flatten.toVector
    randomTarget(mbtargets)
  }

  private def randomTarget(from: Vector[MapObject]): MapObject = {
    val rnd = Random.nextInt(from.size)
    from(rnd)
  }

  private def attack(attacker: Troop, target: MapObject): (Troop, MapObject) = {
    attacker.attack(target)
  }

  val troops = combatants.free

}

object Battle {
  def apply(pos: Coordinate, troops: NESeq[MapObject]): Battle =
    Battle(pos, Combatants(troops, Vector.empty), Round(1, NESeq(troops)))

  def rec_tick(progress: ProgressTime, battle: Option[Battle], leavers: Vector[MapObject]): (Option[Battle], Vector[MapObject], ProgressTime) = {
    battle match {
      case Some(b) =>
        val currentProgress = b.round.progress + progress
        if (currentProgress >= b.round.duration) {
          val (mb, nextLeavers) = b.toNextRound
          rec_tick(currentProgress - b.round.duration, mb, leavers ++ nextLeavers)
        }
        else (Some(b.copy(round = b.round.tick(progress))), leavers, 0)
      case _ => (None, leavers, progress) //TODO persist battle result
    }
  }
}
