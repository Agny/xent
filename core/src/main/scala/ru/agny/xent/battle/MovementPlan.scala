package ru.agny.xent.battle

import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.unit.Speed.Distance
import ru.agny.xent.core.unit.{Distance, Speed}

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

  private var idx = 0
  private var lastStep = steps.head
  private var isGoingHome = false

  def tick(duration: Distance): Coordinate = move(duration).pos()

  private def move(distance: Distance) = {

    def rec(current: Step, remainder: Distance): Step = current.tick(remainder) match {
      case (step, dist) if isDestinationReached(step, idx) => getHomePath(step, dist)
      case (step, dist) if step.isComplete =>
        idx = idx + 1
        rec(next(step, idx), dist)
      case (step, _) => step
    }

    lastStep = rec(lastStep, distance)
    lastStep
  }

  private def isDestinationReached(current: Step, idx: Int): Boolean = {
    isGoingHome = current.isComplete && idx == steps.length
    isGoingHome
  }

  private def getHomePath(lastStep: Step, traveled: Distance): Step = {
    if (isGoingHome) Movement(lastStep.pos(), steps.head.pos(), traveled)
    else lastStep
  }

  private def next(current: Step, idx: Int): Step =
    if (steps.isDefinedAt(idx)) steps(idx)
    else current
}
