package ru.agny.xent.battle

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.utils.TimeUnit

class MovementTest extends FlatSpec with Matchers with EitherValues {

  "Movement" should "change pos by time" in {
    val from = Coordinate(0, 0)
    val to = Coordinate(5, 10)
    val m = Movement(from, to, System.currentTimeMillis())

    val res = m.pos(10, System.currentTimeMillis() + TimeUnit.hour)
    val expected = Coordinate(5, 5)
    res should be(expected)
  }

  it should "change pos only if required amount of time had been passed" in {
    val from = Coordinate(0, 0)
    val to = Coordinate(5, 10)
    val m = Movement(from, to, System.currentTimeMillis())
    val almostHour = 1000 * 60 * 59

    val res = m.pos(7, System.currentTimeMillis() + almostHour)
    val expected = Coordinate(3, 3)
    res should be(expected)
  }

  "Waiting" should "remain the same pos" in {
    val pos = Coordinate(0, 0)
    val m = new Waiting(pos, System.currentTimeMillis())

    val res = m.pos(21, System.currentTimeMillis() + TimeUnit.hour)
    val expected = pos
    res should be(expected)
  }

}
