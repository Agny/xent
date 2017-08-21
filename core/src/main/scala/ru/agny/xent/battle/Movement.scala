package ru.agny.xent.battle

import ru.agny.xent.core.unit.Speed
import Speed._
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress._

case class Movement(from: Coordinate, to: Coordinate) extends Step {
  override val isBusy = true
  private val path = from.path(to)

  private var traveled: Distance = 0
  private var currentPathIdx = 0

  override def pos(distance: Distance): (Coordinate, Distance) = {
    traveled = traveled + distance
    val (tiles, excessive) = tilesWithRemainder(traveled)
    currentPathIdx = tiles
    currentPathIdx - path.tiles match {
      case over if over >= 0 => finishStep(over, excessive)
      case ok => (path.probe(currentPathIdx), 0)
    }
  }

  private def finishStep(tilesWalkedOver: Int, distanceOfLastTile: Distance) = {
    (path.probe(currentPathIdx), tileToDistance(tilesWalkedOver) + distanceOfLastTile)
  }

  override def isComplete = traveled >= tileToDistance(path.tiles)
}

class Waiting(pos: Coordinate, start: ProgressTime = System.currentTimeMillis()) extends Movement(pos, pos) {
  override val isBusy = false
}
