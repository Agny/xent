package ru.agny.xent.battle

import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.unit.Speed.Distance
import ru.agny.xent.core.unit.{Distance, Speed}

case class MovementPlan(steps: Vector[Step], home: Coordinate) {

  import Speed._

  private val state = StepsView(steps)

  def now(speed: Speed, time: ProgressTime): Coordinate = state.tick(speed in time)

  def goHome(speed: Speed, time: ProgressTime): Coordinate = state.tick(speed in time, turnToHome = true)
}

object MovementPlan {
  val finishingLag = Distance.centerOfTile

  def idle(pos: Coordinate): MovementPlan = MovementPlan(Vector(Movement(pos, pos)), pos)
}

private case class StepsView(steps: Vector[Step]) {

  private var idx = 0
  private var lastStep = steps.head
  private var onWayHome = false

  def tick(distance: Distance, turnToHome: Boolean = false): Coordinate = {
    val res = (turnToHome, onWayHome) match {
      case alreadyOnWay@(true, true) => move(lastStep, distance)
      case justSwitched@(true, false) =>
        onWayHome = true
        move(getHomePath(lastStep), distance)
      case normal@(false, _) => move(lastStep, distance)
    }
    res.pos()
  }

  private def move(last: Step, distance: Distance) = {

    def rec(current: Step, remainder: Distance): Step = current.tick(remainder) match {
      case (step, dist) if isDestinationReached(step, idx) =>
        onWayHome = true
        rec(getHomePath(step), dist)
      case (step, dist) if step.isComplete =>
        idx = idx + 1
        rec(next(step, idx), dist)
      case (step, _) => step
    }

    lastStep = rec(last, distance)
    lastStep
  }

  private def isDestinationReached(current: Step, idx: Int): Boolean = current.isComplete && idx == steps.length

  private def getHomePath(lastStep: Step): Step = Movement(lastStep.pos(), steps.head.pos())

  private def next(current: Step, idx: Int): Step =
    if (steps.isDefinedAt(idx)) steps(idx)
    else current
}