package ru.agny.xent.battle

import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.unit.Occupation
import ru.agny.xent.core.unit.Speed.Speed

case class MovementPlan(steps: Vector[Step], home: Step, start: ProgressTime = System.currentTimeMillis()) extends Occupation {
  override def pos(speed: Speed, time: ProgressTime): Coordinate = {
    steps.find(!_.condition.isMet(time)).map(x => x.pos(speed, time)) getOrElse home.pos(speed, time)
  }
}
