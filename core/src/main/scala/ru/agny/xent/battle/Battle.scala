package ru.agny.xent.battle

import ru.agny.xent.UserType.{ObjectId, UserId}
import ru.agny.xent.battle.unit.Speed.Speed
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.utils.TimeUnit

import scala.collection.mutable
import scala.util.Random

case class Battle(pos: Coordinate, troops: Map[Troop, Occupation], queue: Map[Troop, Occupation], start: ProgressTime, round: Round) extends Occupation {
  private type TO = (Troop, Occupation)
  private val TOMap = troops.map(x => x._1.id -> x._2)

  def tick: (Option[Battle], Vector[TO]) = {
    val now = System.currentTimeMillis()
    val timeRemains = (start + round.time) - now
    if (timeRemains <= 0) toNextRoundM
    else (Some(this), Vector.empty)
  }

  def addTroops(t: Map[Troop, Occupation]): Battle = copy(queue = queue ++: t)

  //TODO unite troops to Squads
  private def toNextRoundM: (Option[Battle], Vector[TO]) = {
    val troopsPool = mutableGroupByUsers(troops.keys)
    val troopsResult = nextAttack(troopsPool.values.flatten.unzip._2.toVector, troopsPool)
    val (fallen, alive) = sendFallenToHome(troopsResult)
    val (exhausted, fresh) = freeExhaustedFromBattle(alive)
    val out = fallen ++ exhausted
    val ready = resumePreviousOccupation(fresh)
    if (fresh.isEmpty || isSameSide(queue.keys.toVector ++ fresh)) (None, out ++ ready ++ queue)
    else {
      val toBattle = queue ++ ready
      val r = Round(round.n + 1, toBattle.keys)
      (Some(Battle(pos, toBattle, Map.empty, System.currentTimeMillis(), r)), out)
    }
  }

  private def mutableGroupByUsers(troops: Iterable[Troop]): Map[UserId, mutable.Map[ObjectId, Troop]] = {
    val empty = Map.empty[UserId, mutable.Map[ObjectId, Troop]]
    troops.foldLeft(empty)((m, t) => m.updated(t.user, m(t.user).updated(t.id, t)))
  }

  private def nextAttack(attackers: Vector[Troop], pool: Map[UserId, mutable.Map[ObjectId, Troop]]): Vector[Troop] = {
    val (mostInitiative +: _) = attackers.sortBy(_.initiative)
    val target = claimEnemy(mostInitiative.user, pool.map(x => x._1 -> x._2.values))
    val (attacker, attacked) = attack(mostInitiative, target)

    pool(attacker.user).update(attacker.id, attacker)
    pool(attacked.user).update(attacked.id, attacked)

    val canAttack = pool.updated(attacker.user, pool(attacker.user) - attacker.user).values.flatten.unzip._2.toVector
    if (isNextAttackAvailable(canAttack, pool)) nextAttack(canAttack, pool) else pool.values.flatten.unzip._2.toVector
  }

  private def isNextAttackAvailable(attackers: Vector[Troop], pool: Map[UserId, mutable.Map[ObjectId, Troop]]) = {
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

  //TODO calculate home coordinates for fallen
  private def sendFallenToHome(troops: Vector[Troop]): (Vector[TO], Vector[Troop]) = {
    val (fallen, alive) = troops.partition(_.isActive)
    (fallen.map(x => x -> TOMap(x.id)), alive)
  }

  private def freeExhaustedFromBattle(troops: Vector[Troop]): (Vector[TO], Vector[Troop]) = {
    val (fresh, exhausted) = troops.partition(_.endurance > 0)
    (resumePreviousOccupation(exhausted), fresh)
  }

  private def isSameSide(v: Vector[Troop]) = v.map(_.user).distinct.length < 2

  private def resumePreviousOccupation(troops: Vector[Troop]): Vector[TO] = troops.map(x => x -> TOMap(x.id))

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
  def apply(pos: Coordinate, troops: Map[Troop, Occupation]): Battle = Battle(pos, troops, Map.empty, System.currentTimeMillis(), Round(1, troops.keys))
}

object Round {
  val timeLimitMax = TimeUnit.minute * 10
  val timeLimitMin = TimeUnit.minute
}
