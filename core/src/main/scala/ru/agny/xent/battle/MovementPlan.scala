package ru.agny.xent.battle

import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.unit.Speed.Distance
import ru.agny.xent.core.unit.{Distance, Occupation, Speed}

case class MovementPlan(steps: Vector[Step], home: Coordinate) {

  import Speed._

  private val state = StepsView(steps)

  def now(speed: Speed, time: ProgressTime): Coordinate = state.tick(speed in time)

}

object MovementPlan {
  val finishingLag = Distance.centerOfTile

  def idle(pos: Coordinate): MovementPlan = MovementPlan(Vector(Movement(pos, pos)), pos)
}

case class StepsView(steps: Vector[Step]) {

  import MovementPlan._

  private var idx = 0
  private var lastStep: Occupation = steps.head
  private lazy val homePath = Movement(lastStep.pos(), steps.head.pos())

  def tick(duration: Distance): Coordinate = move(duration).pos()

  private def move(distance: Distance) = {

    def rec(remainder: Distance): Occupation = lastStep.tick(remainder) match {
      case (_, dist) if dist > finishingLag =>
        idx = idx + 1
        lastStep = next()
        rec(dist - finishingLag)
      case (occupation, dist) =>
        lastStep = occupation
        occupation
    }

    rec(distance)
  }

  private def next(): Occupation = if (steps.isDefinedAt(idx)) steps(idx) else homePath
}
