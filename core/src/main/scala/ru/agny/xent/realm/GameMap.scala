package ru.agny.xent.realm

import ru.agny.xent.item.{DestructibleObject, MapObject, TemporalObject}
import ru.agny.xent.realm.GameMap.{DestructibleTickResult, BattleTickResult}
import ru.agny.xent.TimeInterval
import ru.agny.xent.realm.Realm.{Strong, Weak}
import ru.agny.xent.realm.map.Battle.Preparation
import ru.agny.xent.realm.map.{Battle, Troops}
import GameMap._

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
  private val battleRelated: Seq[TemporalObject]
) {
  private val (minX, minY) = (-maxX, -maxY)
  private var temporal: Seq[TemporalObject] = battleRelated
  private var places: Map[Hexagon, Seq[DestructibleObject]] = objects.groupMap(_.pos)(x => x)

  def tick(timer: Realm.Timer): Unit = {
    timer.tick() match {
      case Weak(volume) =>
        val BTR(troops) = GameMap.tick(temporal, places, volume)

        temporal = troops
      case Strong(volume, accumulated) =>
        val DTR(updatedPlaces) = GameMap.tick(places.values.flatten.toSeq, accumulated)
        val BTR(troops) = GameMap.tick(temporal, places, volume)

        temporal = troops 
        places = updatedPlaces.groupMap(_.pos)(x => x)
    }
  }

  def getState(): Seq[DestructibleObject] = places.values.flatten.toSeq

  def getTroops(): Seq[TemporalObject] = temporal
}

object GameMap {
  case class DestructibleTickResult(remains: Seq[DestructibleObject])
  case class BattleTickResult(moving: Seq[TemporalObject])
  type DTR = DestructibleTickResult
  type BTR = BattleTickResult

  private def tick(
    objects: Seq[DestructibleObject],
    time: TimeInterval
  ): DestructibleTickResult = {
    val (operational, eliminated) = objects.partition { x =>
      val updated = x.tick(time)
      !updated.isEliminated()
    }
    val cleared = eliminated flatMap {
      case _ => Seq.empty[DestructibleObject] // remnants?
    }
    DestructibleTickResult(operational)
  }

  private def tick(
    temporals: Seq[TemporalObject],
    objects: Map[Hexagon, Seq[DestructibleObject]],
    time: TimeInterval
  ): BattleTickResult = {
    val movingPos = temporals.groupMap { x =>
      x.tick(time)
      x.pos
    } { x => x }

    val m = mutable.Seq.empty
    movingPos.foreach { case (pos, xs) =>
      val Preparation(updatedPlaces, nonparticipants) = Battle.build(xs, objects.getOrElse(pos, Seq.empty))
      updatedPlaces ++: nonparticipants ++: m
    }
    BattleTickResult(m.toSeq)
  }
}