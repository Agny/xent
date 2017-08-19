package ru.agny.xent.battle

import ru.agny.xent.core.unit.Speed
import Speed._
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress._

case class Movement(from: Coordinate, to: Coordinate, start: ProgressTime = System.currentTimeMillis()) extends Step {
  override val isBusy = true
  private val path = from.path(to)

  override def pos(by: Speed, current: ProgressTime): Coordinate = {
    val elapsed = current - start
    val distance = by in elapsed
    path.probe(distance)
  }

  override val condition = ???
}

class Waiting(pos: Coordinate, start: ProgressTime = System.currentTimeMillis()) extends Movement(pos, pos, start) {
  override val isBusy = false
}
