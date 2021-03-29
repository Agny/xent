package ru.agny.xent.realm

import ru.agny.xent.item.{DestructibleObject, MapObject, MovingObject}
import ru.agny.xent.realm.GameMap.{DestructibleTickResult}
import ru.agny.xent.TimeInterval
import ru.agny.xent.realm.Realm.{Strong, Weak}
import ru.agny.xent.realm.map.{Battle, Troops}

import scala.collection.mutable

/**
 * Rectangular-like map of a realm with a center at (0,0)
 * It contains mutable state of buildings/armies/etc
 *
 * @param maxX maximum X range (absolute value)
 * @param maxY maximum Y range (absolute value)
 */
class GameMap(
  maxX: Int,
  maxY: Int,
  objects: Seq[DestructibleObject],
  troops: Seq[MovingObject]
) {
  private val (minX, minY) = (-maxX, -maxY)
  private val moving: mutable.Seq[MovingObject] = mutable.Seq.from(troops)
  private val clashes: mutable.Map[Hexagon, mutable.Seq[MapObject]] = mutable.Map.empty

  def tick(timer: Realm.Timer): Unit = {
    timer.tick() match {
      case Weak(volume) =>
      case Strong(volume, accumulated) =>
        val dTicked = GameMap.tick(objects, accumulated)
        val updated = moving.map { x =>
          x.tick(volume)
          if (clashes.isDefinedAt(x.pos)) {
            x +: clashes(x.pos)
          } else {
            clashes += (x.pos -> mutable.Seq(x))
          }
        }
        ???

    }
  }
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
    val r = objects.partition { x =>
      val updated = x.tick(time)
      !updated.isEliminated()
    }
    val cleared = r._2 flatMap {
      case b: Battle => b.end()
      case _ => Seq.empty // remnants?
    }
    DestructibleTickResult(r._1, cleared)
  }
}