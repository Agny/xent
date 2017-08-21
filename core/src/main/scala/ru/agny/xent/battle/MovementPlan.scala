package ru.agny.xent.battle

import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.unit.Speed

case class MovementPlan(steps: Vector[Step], home: Coordinate) {

  import Speed._

  private var lastPos = home
  private val finishingLag = tileToDistance(1) / 2 //TODO add lag to step switch
  private lazy val homePath = Movement(lastPos, home)

  def now(speed: Speed, time: ProgressTime): Coordinate = {
    move(steps, speed in time) match {
      case (Some(c), traveled) => c
      case (None, traveled) => goHome(speed, traveled / speed)
    }
  }

  def goHome(speed: Speed, time: ProgressTime): Coordinate = homePath.pos(speed in time)._1

  private def move(steps: Vector[Step], distance: Distance) = {
    def rec(remainder: Distance): (Option[Coordinate], Distance) = steps.find(!_.isComplete).map(_.pos(remainder)) match {
      case Some((c, p)) if p > 0 =>
        lastPos = c
        rec(remainder - p)
      case Some((c, d)) =>
        lastPos = c
        (Some(c), d)
      case None => (None, remainder)
    }

    rec(distance)
  }
}

object MovementPlan {
  def idle(pos: Coordinate): MovementPlan = MovementPlan(Vector.empty, pos)
}
