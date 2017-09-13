package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.{Coordinate, MapObject}
import ru.agny.xent.core.Progress._
import ru.agny.xent.core.utils.NESeq

import scala.annotation.tailrec

case class Military(objects: Vector[MapObject], events: Vector[Event], lastTick: ProgressTime) {

  import ru.agny.xent.battle.Military._

  def tick(from: ProgressTime = System.currentTimeMillis()): (Military, Vector[MapObject]) = {
    val progress = from - lastTick
    val updated = quantify(this, progress, Round.timeLimitMin - 1)
    updated.discardFallen()
  }

  private def discardFallen(): (Military, Vector[MapObject]) = {
    val (fallen, actual) = objects.partition(x => x.isDiscardable)
    (copy(objects = actual), fallen)
  }

}

object Military {

  val empty = Military(Vector.empty, Vector.empty, System.currentTimeMillis())

  @tailrec private def quantify(m: Military, time: ProgressTime, by: ProgressTime): Military = {
    val quantum = if (time > by) by else time
    val (battleActive, fallen) = m.objects.map(x => (x, x.pos(quantum))).partition(_._1.isActive)
    val grouped = groupByPos(battleActive)
    val (freeTroops, updatedEvents) = collide(m.events, grouped, quantum)
    val res = Military(freeTroops ++ fallen.unzip._1, updatedEvents, m.lastTick + quantum)

    if (time > by) {
      quantify(res, time - by, by)
    } else {
      res
    }
  }

  private def groupByPos(objects: Vector[(MapObject, Coordinate)]) = {

    def addPos(ct: Map[Coordinate, Vector[MapObject]], pos: Coordinate, t: MapObject) = ct.updated(pos, t +: ct(pos))

    val empty = Map.empty[Coordinate, Vector[MapObject]].withDefaultValue(Vector.empty)
    objects.foldLeft(empty) { (positioned, t) =>
      addPos(positioned, t._2, t._1)
    }
  }

  private def collide(ongoingEvents: Vector[Event], positioned: Map[Coordinate, Vector[MapObject]], time: ProgressTime): (Vector[MapObject], Vector[Event]) = {
    val (updatedEvents, outgoingTroops) = ongoingEvents.foldLeft(Vector.empty[Event], Vector.empty[MapObject]) {
      case ((events, troops), event) => event.tick(time) match {
        case (Some(b: Battle), out, _) =>
          val troopsInPos = positioned(b.pos).collect { case t: Troop => t }
          (b.addTroops(troopsInPos.filter(_.isAggressive)) +: events, out ++ troops)
        case (Some(other), out, _) => (other +: events, out ++ troops)
        case (_, freed, progress) =>
          freed.foreach(_.pos(progress))
          (events, freed ++ troops)
      }
    }

    val (newEvents, movingTroops) = positioned.filterKeys(x => !updatedEvents.exists(_.pos == x)).foldLeft(Vector.empty[Event], Vector.empty[MapObject]) {
      case ((events, troops), (x, ts)) if Combatants.isBattleNeeded(ts) => (Battle(x, NESeq(ts)) +: events, troops)
      case ((events, troops), (x, ts)) => (events, troops ++ ts)
    }
    (outgoingTroops ++ movingTroops, updatedEvents ++ newEvents)
  }
}
