package ru.agny.xent.battle

import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.unit.Speed.Speed

case class MovementPlan(steps: Vector[Step], home: Coordinate, start: ProgressTime = System.currentTimeMillis()) {
  def now(speed: Speed, time: ProgressTime): Coordinate = {
    steps.find(!_.condition.isMet(time)).map(x => x.pos(speed, time)) getOrElse goHome().pos(speed, time)
  }

  private def goHome(): Step = ???

  private def lastPos(): Coordinate = ???
}

object MovementPlan {
  def idle(pos: Coordinate): MovementPlan = MovementPlan(Vector.empty, pos)
}
