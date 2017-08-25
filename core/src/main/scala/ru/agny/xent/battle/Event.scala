package ru.agny.xent.battle

import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime

trait Event {
  val pos: Coordinate

  def tick(from: ProgressTime): (Option[Event], Vector[Troop], ProgressTime)
}
