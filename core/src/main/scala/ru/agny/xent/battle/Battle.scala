package ru.agny.xent.battle

import ru.agny.xent.UserType.UserId
import ru.agny.xent.battle.unit.Speed.Speed
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.utils.NESeq

import scala.annotation.tailrec
import scala.util.Random

case class Battle(pos: Coordinate, private val combatants: Combatants, start: ProgressTime, round: Round) extends Occupation {

  import Combatants._

  def tick(implicit from: ProgressTime = System.currentTimeMillis()): (Option[Battle], Vector[TO]) = {
    val timePassed = from - (start + round.duration)
    if (timePassed > 0) toNextRound(timePassed, Vector.empty)
    else (Some(this), Vector.empty)
  }

  def addTroops(t: Vector[(Troop, Occupation)]): Battle = copy(combatants = combatants.queue(t))

  //TODO unite troops to Squads
  @tailrec private def toNextRound(remainder: ProgressTime, outs: Vector[TO]): (Option[Battle], Vector[TO]) = {
    val troopsByUser = combatants.groupByUsers
    val (troopsResult, _) = nextAttack(troopsByUser.values.flatten.unzip._2.toVector, troopsByUser)
    val (next, out) = prepareToNextRound(combatants, troopsResult)
    next match {
      case Some(v) =>
        val r = Round(round.n + 1, NESeq(v.troops.unzip._1))
        val nextBattleRound = Battle(pos, v, start + round.duration, r)
        val isAnotherRoundPassed = (r.duration - remainder) < 0
        if (isAnotherRoundPassed) {
          nextBattleRound.toNextRound(remainder - r.duration, outs ++ out)
        } else {
          (Some(nextBattleRound), outs ++ out)
        }
      case None => (None, outs ++ out)
    }
  }

  @tailrec private def nextAttack(attackers: Vector[Troop], pool: Pool): (Vector[Troop], Pool) = {
    val (mostInitiative +: tail) = attackers.sortBy(_.initiative)
    val target = claimEnemy(mostInitiative.user, pool.map(x => x._1 -> x._2.values))
    val (attacker, attacked) = attack(mostInitiative, target)
    val poolWithAttacker = Combatants.adjustPool(pool, attacker)
    val updPool = Combatants.adjustPool(poolWithAttacker, attacked)

    val nextAttackers = validateAttackers(tail, attacked)
    if (isNextAttackAvailable(nextAttackers, updPool)) nextAttack(nextAttackers, updPool)
    else (updPool.values.flatten.unzip._2.toVector, updPool)
  }

  private def validateAttackers(base: Vector[Troop], attacked: Troop): Vector[Troop] = {
    val others = base.filterNot(_.id == attacked.id)
    if (others.size == base.size) others
    else {
      if (attacked.isAbleToFight) attacked +: others
      else others
    }
  }

  private def isNextAttackAvailable(attackers: Vector[Troop], pool: Pool) = {
    if (attackers.isEmpty) false
    else {
      val possibleTargets = attackers.flatMap(x => pool.filter(_._1 != x.user).values.flatten.unzip._2)
      possibleTargets.map(_.isActive).nonEmpty
    }
  }

  private def claimEnemy(self: UserId, troops: Map[UserId, Iterable[Troop]]): Troop = {
    val mbtargets = troops.filter(_._1 != self).values.flatten.toVector
    randomTarget(mbtargets)
  }

  private def randomTarget(from: Vector[Troop]): Troop = {
    val rnd = Random.nextInt(from.size)
    from(rnd)
  }

  private def attack(attacker: Troop, target: Troop): (Troop, Troop) = {
    attacker.attack(target)
  }

  val troops = combatants.free.unzip._1

  override val isBusy = true

  override def pos(speed: Speed, time: ProgressTime): Coordinate = pos
}

object Battle {
  def apply(pos: Coordinate, troops: NESeq[(Troop, Occupation)])(implicit from: ProgressTime = System.currentTimeMillis()): Battle = {
    val now = System.currentTimeMillis()
    Battle(pos, Combatants(troops, Vector.empty), now - (from - now), Round(1, NESeq(troops.unzip._1)))
  }
}
