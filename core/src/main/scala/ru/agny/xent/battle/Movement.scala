package ru.agny.xent.battle

import ru.agny.xent.core.unit.{Distance, Speed}
import Speed._
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress._

case class Movement(from: Coordinate, to: Coordinate, traveled: Distance = 0) extends Step {
  override val isBusy = true
  private val path = from.path(to)
  private val distanceLength = Distance.tileToDistance(path.tiles) + Distance.centerOfTile

  override def pos(): Coordinate = path.probe(Distance.tiles(traveled))

  override def tick(distance: Distance): (Movement, Distance) = {
    val updatedTraveled = traveled + distance
    if (updatedTraveled > distanceLength)
      (copy(traveled = distanceLength), updatedTraveled - distanceLength)
    else (copy(traveled = updatedTraveled), 0)
  }

  override def isComplete = traveled >= distanceLength
}

class Waiting(pos: Coordinate, start: ProgressTime = System.currentTimeMillis()) extends Movement(pos, pos, 0) {
  override val isBusy = false
}
