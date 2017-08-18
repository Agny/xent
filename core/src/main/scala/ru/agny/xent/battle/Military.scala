package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress._
import ru.agny.xent.core.unit.{Occupation}
import ru.agny.xent.core.utils.{NESeq, SubTyper}

import scala.annotation.tailrec

case class Military(troops: Vector[Troop], events: Vector[Event]) {

  import ru.agny.xent.battle.Military._

  def tick(from: ProgressTime = System.currentTimeMillis()): (Military, Vector[Troop]) = {
    val steps = (from - System.currentTimeMillis()) / Round.timeLimitMin
    quantify(this, from, steps, Round.timeLimitMin)
  }

}

object Military {

  private type TB = (Troop, Battle)

  val empty = Military(Vector.empty, Vector.empty)

  @tailrec private def quantify(m: Military, time: ProgressTime, stepsRemains: Long, by: ProgressTime): Military = {
    val quantum = time - stepsRemains * by
    val troopsPositions = moveTick(m.troops)(quantum)
    val (freeTroops, updatedEvents) = collide(m.events, troopsPositions, quantum)
    val res = Military(freeTroops, updatedEvents)

    if (stepsRemains > 0) {
      quantify(res, time, stepsRemains - 1, by)
    } else {
      res
    }
  }

  private def moveTick(troops: Vector[Troop])(time: ProgressTime) = {

    def addPos(ct: Map[Coordinate, Vector[Troop]], pos: Coordinate, t: Troop) = ct.updated(pos, t +: ct(pos))

    val empty = Map.empty[Coordinate, Vector[Troop]].withDefaultValue(Vector.empty)
    troops.foldLeft(empty) { (positioned, t) =>
      val pos = t.pos.now(t.moveSpeed, time)
      addPos(positioned, pos, t)
    }
  }

  private def collide(ongoingEvents: Vector[Event], positioned: Map[Coordinate, Vector[Troop]], time: ProgressTime): (Vector[Troop], Vector[Event]) = {
    ongoingEvents.map {
      case b: Battle => b.tick()
    }
  }

  //    positioned.foldLeft(Vector.empty[TM], Vector.empty[Troop]) { (a, b) =>
  //      val (inBattle, out) = findBattle(b._1, b._2, time)
  //      val (alive, fallenArrived) = arrive(out, time)
  //      (alive ++ inBattle ++ a._1, fallenArrived ++ a._2)
  //    }

  private def findBattle(pos: Coordinate, inArea: Vector[TM], time: ProgressTime): (Iterable[TB], Iterable[TM]) = inArea match {
    case multiple@_ +: t if t.nonEmpty =>
      val (inBattle, queueing) = SubTyper.partition[Battle, Occupation, Troop](multiple)
      val (battle, moving) = battleTick(pos, inBattle, queueing, time)
      val tb = battle match {
        case Some(b) => b.troops.map(x => x -> b)
        case _ => Vector.empty
      }
      (tb, moving)
    case x => (Vector.empty, x)
  }

  private def battleTick(pos: Coordinate, inBattle: Vector[TB], queueing: Vector[TM], time: ProgressTime): (Option[Battle], Vector[TM]) = inBattle match {
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
  private def arrive(troops: Iterable[TM], time: ProgressTime): (Vector[TM], Vector[Troop]) = {
    def isFallenAndArrived(t: Troop, m: MovementPlan) = !t.isActive && m.now(Troop.FALLEN_SPEED, time) == m.home

    troops.foldLeft(Vector.empty[TM], Vector.empty[Troop]) { (res, x) =>
      x match {
        case (fallen, m: MovementPlan) if isFallenAndArrived(fallen, m) => (res._1, fallen +: res._2)
        case _ => (x +: res._1, res._2)
      }
    }
  }
}
