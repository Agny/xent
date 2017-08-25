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
    val (fallen, alive) = troops.partition(x => !x.isActive && x.move(0) == x.home)
    (copy(troops = alive), fallen)
  }

}

object Military {

  val empty = Military(Vector.empty, Vector.empty, System.currentTimeMillis())

  @tailrec private def quantify(m: Military, time: ProgressTime, by: ProgressTime): Military = {
    val quantum = if (time > by) by else time
    val (battleActive, fallen) = m.troops.map(x => (x, x.move(quantum))).partition(_._1.isActive)
    val grouped = groupByPos(battleActive)
    val (freeTroops, updatedEvents) = collide(m.events, grouped, quantum)
    val res = Military(freeTroops ++ fallen.unzip._1, updatedEvents, m.lastTick + quantum)

    if (time > by) {
      quantify(res, time - by, by)
    } else {
      res
    }
  }

  private def groupByPos(troops: Vector[(Troop, Coordinate)]) = {

    def addPos(ct: Map[Coordinate, Vector[Troop]], pos: Coordinate, t: Troop) = ct.updated(pos, t +: ct(pos))

    val empty = Map.empty[Coordinate, Vector[Troop]].withDefaultValue(Vector.empty)
    troops.foldLeft(empty) { (positioned, t) =>
      addPos(positioned, t._2, t._1)
    }
  }

  private def collide(ongoingEvents: Vector[Event], positioned: Map[Coordinate, Vector[Troop]], time: ProgressTime): (Vector[Troop], Vector[Event]) = {
    val (updatedEvents, outgoingTroops) = ongoingEvents.foldLeft(Vector.empty[Event], Vector.empty[Troop]) {
      case ((events, troops), event) => event.tick(time) match {
        case (Some(b: Battle), out, _) => (b.addTroops(positioned(b.pos)) +: events, out ++ troops)
        case (Some(other), out, _) => (other +: events, out ++ troops)
        case (_, freed, progress) =>
          freed.foreach(_.move(progress))
          (events, freed ++ troops)
      }
    }

    val (newEvents, movingTroops) = positioned.filterKeys(x => !updatedEvents.exists(_.pos == x)).foldLeft(Vector.empty[Event], Vector.empty[Troop]) {
      case ((events, troops), (x, ts)) if Combatants.isBattleNeeded(ts) => (Battle(x, NESeq(ts)) +: events, troops)
      case ((events, troops), (x, ts)) => (events, troops ++ ts)
    }
    (outgoingTroops ++ movingTroops, updatedEvents ++ newEvents)
  }
}
