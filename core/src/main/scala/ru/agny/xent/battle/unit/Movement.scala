package ru.agny.xent.battle.unit

import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime

case class Movement(from: Coordinate, to: Coordinate, start: ProgressTime) {

  import Speed._

  private val path = Coordinate.path(from, to)

  def move(t: Troop, current: ProgressTime): Coordinate = {
    val elapsed = current - start
    val distance = t.moveSpeed in elapsed
    path.probe(distance)
  }
}
