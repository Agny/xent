package ru.agny.xent.battle

import ru.agny.xent.core.{Coordinate, MapObject}
import ru.agny.xent.core.Progress.ProgressTime

trait Event {
  val pos: Coordinate

  def tick(from: ProgressTime): (Option[Event], Vector[MapObject], ProgressTime)
}
