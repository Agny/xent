package ru.agny.xent.battle

import ru.agny.xent.UserType.UserId
import ru.agny.xent.battle.unit.Speed.Speed
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.utils.TimeUnit

import scala.util.Random

case class Battle(pos: Coordinate, private val combatants: Combatants, start: ProgressTime, round: Round) extends Occupation {

  import Combatants._

  def tick: (Option[Battle], Vector[TO]) = {
    val now = System.currentTimeMillis()
    val timeRemains = (start + round.time) - now
    if (timeRemains <= 0) toNextRound
    else (Some(this), Vector.empty)
  }

  def addTroops(t: Vector[(Troop, Occupation)]): Battle = copy(combatants = combatants.queue(t))

  //TODO unite troops to Squads
  private def toNextRound: (Option[Battle], Vector[TO]) = {
    val troopsByUser = combatants.groupByUsers
    val (troopsResult, _) = nextAttack(troopsByUser.values.flatten.unzip._2.toVector, troopsByUser)
    val (next, out) = nextRound(combatants, troopsResult)
    if (next.isBattleNeeded) {
      val r = Round(round.n + 1, next.troops.unzip._1)
      (Some(Battle(pos, next, System.currentTimeMillis(), r)), out)
    } else {
      (None, next.free)
    }
  }

  private def nextAttack(attackers: Vector[Troop], pool: Pool): (Vector[Troop], Pool) = {
    val (mostInitiative +: _) = attackers.sortBy(_.initiative)
    val target = claimEnemy(mostInitiative.user, pool.map(x => x._1 -> x._2.values))
    val (attacker, attacked) = attack(mostInitiative, target)
    val poolWithAttacker = Combatants.adjustPool(pool, attacker)
    val updPool = Combatants.adjustPool(poolWithAttacker, attacked)

    val canAttack = pool.updated(attacker.user, pool(attacker.user) - attacker.user).values.flatten.unzip._2.toVector
    if (isNextAttackAvailable(canAttack, updPool)) nextAttack(canAttack, updPool) else (updPool.values.flatten.unzip._2.toVector, updPool)
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

case class Round(n: Int, troops: Iterable[Troop]) {

  import Round._

  val time: ProgressTime = {
    val byUsers = Troop.groupByUsers(troops)
    time(byUsers.map { case (uid, ts) => uid -> ts.foldLeft(0)((w, t) => w + weight(t)) }.values)
  }

  private def weight(t: Troop): Int = troops.foldLeft(0)((sum, x) => sum + x.weight)

  private def time(armiesWithWeight: Iterable[Int]): ProgressTime = {
    val max = armiesWithWeight.max
    val min = armiesWithWeight.min
    math.round((min.toDouble / max) * timeLimitMax)
  }
}

object Battle {
  def apply(pos: Coordinate, troops: Vector[(Troop, Occupation)]): Battle = Battle(pos, Combatants(troops, Vector.empty), System.currentTimeMillis(), Round(1, troops.unzip._1))
}

object Round {
  val timeLimitMax = TimeUnit.minute * 10
  val timeLimitMin = TimeUnit.minute
}
