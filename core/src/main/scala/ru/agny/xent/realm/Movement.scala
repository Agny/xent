package ru.agny.xent.realm

import ru.agny.xent.{Distance, Velocity, TimeInterval}

case class Movement(
  private val path: Path
) {

  private var pos: Hexagon = path.from
  private var passed: Distance = Hexagon.Center
  private var isFinished = false

  def tick(velocity: Velocity, time: TimeInterval): Unit = {
    passed = passed + velocity * time
    if (pos == path.to) {
      val remains = Hexagon.Center - passed
      if (remains < 0) {
        passed = Hexagon.Center
        isFinished = true
      }
    } else {
      val remains = Hexagon.Length - passed
      if (remains < 0) {
        passed = -remains
        pos = path.next()
      }
    }
  }

  def isDestinationReached(): Boolean = isFinished
}
