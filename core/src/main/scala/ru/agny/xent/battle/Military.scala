package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress._
import ru.agny.xent.core.utils.NESeq

import scala.annotation.tailrec

case class Military(troops: Vector[Troop], events: Vector[Event], lastTick: ProgressTime) {

  import ru.agny.xent.battle.Military._

  def tick(from: ProgressTime = System.currentTimeMillis()): (Military, Vector[Troop]) = {
    val progress = from - lastTick
    val updated = quantify(this, progress, Round.timeLimitMin)
    updated.releaseArrivedFallen()
  }

  private def releaseArrivedFallen(): (Military, Vector[Troop]) = {
    val (fallen, alive) = troops.partition(x => !x.isActive && x.move(0) == x.pos.home)
    (copy(troops = alive), fallen)
  }

}

object Military {

  val empty = Military(Vector.empty, Vector.empty, System.currentTimeMillis())

  @tailrec private def quantify(m: Military, time: ProgressTime, by: ProgressTime): Military = {
    val quantum = if (time > by) by else time
    val troopsPositions = moveTick(m.troops)(quantum)
    val (freeTroops, updatedEvents) = collide(m.events, troopsPositions, quantum)
    val res = Military(freeTroops, updatedEvents, m.lastTick + quantum)

    if (time > by) {
      quantify(res, time - by, by)
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
    val (updatedEvents, outgoingTroops) = ongoingEvents.foldLeft(Vector.empty[Event], Vector.empty[Troop]) {
      case ((events, troops), event) => event.tick(time) match {
        case (Some(b: Battle), out) => (b.addTroops(positioned(b.pos)) +: events, out ++ troops)
        case (Some(other), out) => (other +: events, out ++ troops)
        case (_, freed) => (events, freed ++ troops)
      }
    }

    val (newEvents, movingTroops) = positioned.filterKeys(x => !updatedEvents.exists(_.pos == x)).foldLeft(Vector.empty[Event], Vector.empty[Troop]) {
      case ((events, troops), (x, ts)) if Combatants.isBattleNeeded(ts) => (Battle(x, NESeq(ts)) +: events, troops)
      case ((events, troops), (x, ts)) => (events, troops ++ ts)
    }
    (outgoingTroops ++ movingTroops, updatedEvents ++ newEvents)
  }
}
