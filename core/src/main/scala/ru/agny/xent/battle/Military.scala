package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress._
import ru.agny.xent.core.utils.{NESeq, SubTyper}

import scala.annotation.tailrec

case class Military(troops: Vector[(Troop, Occupation)]) {

  import ru.agny.xent.battle.Military._

  def tick(from: ProgressTime = System.currentTimeMillis()): (Military, Vector[Troop]) = {
    val steps = (from - System.currentTimeMillis()) / Round.timeLimitMin
    quantify(this, from, steps, Round.timeLimitMin, Vector.empty)
  }

}

object Military {

  import OccupationSubTyper.implicits._

  private type TO = (Troop, Occupation)
  private type TB = (Troop, Battle)

  val empty = Military(Vector.empty)

  @tailrec private def quantify(m: Military, time: ProgressTime, stepsRemains: Long, by: ProgressTime, erased: Vector[Troop]): (Military, Vector[Troop]) = {
    val quantum = time - stepsRemains * by
    val troopsPositions = moveTick(m.troops)(quantum)
    val (alive, toErase) = collide(troopsPositions, quantum)
    val res = Military(alive)

    if (stepsRemains > 0) {
      quantify(res, time, stepsRemains - 1, by, erased ++ toErase)
    } else {
      (res, erased ++ toErase)
    }
  }

  private def moveTick(troops: Vector[TO])(time: ProgressTime) = {
    val empty = Map.empty[Coordinate, Vector[TO]].withDefaultValue(Vector.empty)
    troops.foldLeft(empty) { (positioned, t) =>
      val pos = t._2.pos(t._1.moveSpeed, time)
      addPos(positioned, pos, t)
    }
  }

  private def collide(positioned: Map[Coordinate, Vector[TO]], time: ProgressTime) =
    positioned.foldLeft(Vector.empty[TO], Vector.empty[Troop]) { (a, b) =>
      val (inBattle, out) = findBattle(b._1, b._2, time)
      val (moving, fallenArrived) = arrive(out, time)
      (moving ++ inBattle ++ a._1, fallenArrived ++ a._2)
    }

  private def addPos(ct: Map[Coordinate, Vector[TO]], pos: Coordinate, t: TO) = ct.updated(pos, t +: ct(pos))

  private def findBattle(pos: Coordinate, inArea: Vector[TO], time: ProgressTime): (Iterable[TB], Iterable[TO]) = inArea match {
    case multiple@h +: t if t.nonEmpty =>
      val (inBattle, queueing) = SubTyper.partition[Battle, Occupation, Troop](multiple)
      val (battle, moving) = battleTick(pos, inBattle, queueing, time)
      val tb = battle match {
        case Some(b) => b.troops.map(x => x -> b)
        case _ => Vector.empty
      }
      (tb, moving)
    case x => (Vector.empty, x)
  }

  private def battleTick(pos: Coordinate, inBattle: Vector[TB], queueing: Vector[TO], time: ProgressTime): (Option[Battle], Vector[TO]) = inBattle match {
    case (_, b) +: _ =>
      val (inProcess, leaving) = b.tick(time)
      inProcess match {
        case Some(battle) => val (toBattle, byPass) = queueing.partition(x => x._1.isAbleToFight)
          (Some(battle.addTroops(toBattle)), leaving ++ byPass)
        case None if Combatants.isBattleNeeded(queueing.unzip._1) => (Some(Battle(pos, NESeq(queueing), time)), leaving)
        case _ => (None, leaving ++ queueing)
      }
    case _ if Combatants.isBattleNeeded(queueing.unzip._1) => Battle(pos, NESeq(queueing), time).tick(time)
    case _ => (None, queueing)
  }

  //TODO send troop back to city upon arriving
  private def arrive(troops: Iterable[TO], time: ProgressTime): (Vector[TO], Vector[Troop]) = troops.foldLeft(Vector.empty[TO], Vector.empty[Troop]) { (res, x) =>
    x match {
      case (fallen, m: Movement) if !fallen.isActive && m.pos(Troop.FALLEN_SPEED, time) == m.to => (res._1, fallen +: res._2)
      case (alive, m: Movement) if m.pos(alive.moveSpeed, time) == m.to => ((alive, new Waiting(m.to, time)) +: res._1, res._2)
      case _ => (x +: res._1, res._2)
    }
  }
}
