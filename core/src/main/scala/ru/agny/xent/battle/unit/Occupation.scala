package ru.agny.xent.battle.unit

import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime
import Speed._

trait Occupation {
  val isBusy = false
}

trait Moving extends Occupation {
  override val isBusy = true

  def run(by: Speed, time: ProgressTime): Coordinate
}
case class Movement(from: Coordinate, to: Coordinate, start: ProgressTime) extends Moving {

  private val path = Coordinate.path(from, to)

  def run(by: Speed, current: ProgressTime): Coordinate = {
    val elapsed = current - start
    val distance = by in elapsed
    path.probe(distance)
  }
}
object Waiting extends Occupation
