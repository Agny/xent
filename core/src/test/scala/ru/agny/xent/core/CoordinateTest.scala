package ru.agny.xent.core

import org.scalatest.{EitherValues, Matchers, FlatSpec}

class CoordinateTest extends FlatSpec with Matchers with EitherValues {

  "Coordinate" should "calculate distance" in {
    val from = Coordinate(0, 0)
    val to = Coordinate(3, 1)
    val res = from.distance(to)
    val expected = 4
    res should be(expected)
  }

  it should "calculate distance to itself as zero" in {
    val pos = Coordinate(0, 0)
    val res = pos.distance(pos)
    val expected = 0
    res should be(expected)
  }
}
