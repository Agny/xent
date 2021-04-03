package ru.agny.xent.realm

import ru.agny.xent.item.{DestructibleObject, MapObject, MovingObject}
import ru.agny.xent.realm.GameMap.DestructibleTickResult
import ru.agny.xent.TimeInterval
import ru.agny.xent.realm.Realm.{Strong, Weak}
import ru.agny.xent.realm.map.Battle.Preparation
import ru.agny.xent.realm.map.{Battle, Troops}

import scala.collection.mutable

/**
 * Rectangular-like map of a realm with a center at (0,0)
 * It contains mutable state of buildings/armies/etc
 *
 * @param maxX maximum X range (absolute value)
 * @param maxY maximum Y range (absolute value)
 */
case class GameMap(
  maxX: Int,
  maxY: Int,
  private val objects: Seq[DestructibleObject],
  private val troops: Seq[MovingObject]
) {
  private val (minX, minY) = (-maxX, -maxY)
  private var moving: Seq[MovingObject] = troops
  private var places: Map[Hexagon, Seq[DestructibleObject]] = objects.groupMap(_.pos)(x => x)

  def tick(timer: Realm.Timer): Unit = {
    timer.tick() match {
      case Weak(volume) =>
        val movingPos = moving.groupMap { x =>
          x.tick(volume)
          x.pos
        } { x => x }

        val p = mutable.Map.from(places)
        val m = mutable.Seq.from(moving)
        movingPos.foreach { case (pos, xs) =>
          val Preparation(updatedPlaces, nonparticipants) = Battle.build(xs, p.getOrElse(pos, Seq.empty))
          p.update(pos, updatedPlaces)
          nonparticipants ++: m
        }
        moving = m.toSeq
        places = p.toMap

      case Strong(volume, accumulated) =>
        val DestructibleTickResult(r, l) = GameMap.tick(places.values.flatten.toSeq, accumulated)

        places = r.groupMap(_.pos)(x => x)

        val movingPos = moving.groupMap { x =>
          x.tick(volume)
          x.pos
        } { x => x }

        val p = mutable.Map.from(places)
        val m = mutable.Seq.from(l)
        movingPos.foreach { case (pos, xs) =>
          val Preparation(updatedPlaces, nonparticipants) = Battle.build(xs, p.getOrElse(pos, Seq.empty))
          p.update(pos, updatedPlaces)
          nonparticipants ++: m
        }

        moving = m.toSeq
        places = p.toMap
    }
  }

  def getState(): Seq[DestructibleObject] = places.values.flatten.toSeq

  def getTroops(): Seq[MovingObject] = moving
}

object GameMap {
  case class DestructibleTickResult(
    remains: Seq[DestructibleObject],
    leftFromBattle: Seq[Troops]
    //other remnants?
  )

  private def tick(
    objects: Seq[DestructibleObject],
    time: TimeInterval
  ): DestructibleTickResult = {
    val (operational, eliminated) = objects.partition { x =>
      val updated = x.tick(time)
      !updated.isEliminated()
    }
    val cleared = eliminated flatMap {
      case b: Battle => b.end()
      case _ => Seq.empty // remnants?
    }
    DestructibleTickResult(operational, cleared)
  }
}