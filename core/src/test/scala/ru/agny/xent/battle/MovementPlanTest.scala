package ru.agny.xent.battle

import org.scalatest.{FlatSpec, Matchers}
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.unit.{Distance, Speed}
import ru.agny.xent.core.utils.TimeUnit

class MovementPlanTest extends FlatSpec with Matchers {

  val home = Coordinate(1, 1)
  val movements = Vector(Movement(home, Coordinate(2, 2)), Movement(Coordinate(2, 2), Coordinate(3, 5)))

  "Movement plan" should "handle lag between steps" in {
    val plan = MovementPlan(movements, home)
    val timePerTile = TimeUnit.hour / Speed.default
    val lastPosOfStep = timePerTile + MovementPlan.finishingLag / Speed.default

    plan.now(Speed.default, timePerTile * 2) should be(Coordinate(2, 2))
    plan.now(Speed.default, lastPosOfStep - 1) should be(Coordinate(2, 2))
    plan.now(Speed.default, 1) should be(Coordinate(2, 3))
  }

  it should "switch destination to home if last step is complete" in {
    val plan = MovementPlan(movements, home)
    val pathToLastPos = {
      val stepSwitchLag = MovementPlan.finishingLag
      val firstStepTiles = Distance.tileToDistance(3)
      val secondStepTilesBeforeLast = Distance.tileToDistance(4)
      val commonTilesInSteps = Distance.tileToDistance(1)
      (firstStepTiles + stepSwitchLag + secondStepTilesBeforeLast - commonTilesInSteps) / Speed.default
    }
    val spentAtLastPos = {
      val stepSwitchToHomePathLag = MovementPlan.finishingLag
      val moveTileOver = Distance.tileToDistance(1)
      (stepSwitchToHomePathLag + moveTileOver) / Speed.default
    }
    plan.now(Speed.default, pathToLastPos) should be(Coordinate(3, 5))
    plan.now(Speed.default, spentAtLastPos - 1) should be(Coordinate(3, 5))
    plan.now(Speed.default, 1) should be(Coordinate(3, 4))
    plan.now(Speed.default, Distance.tileToDistance(6) / Speed.default) should be(Coordinate(1, 1))
  }
}
