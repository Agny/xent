package ru.agny.xent.core

import org.scalatest.{EitherValues, Matchers, FlatSpec}

class PathTest extends FlatSpec with Matchers with EitherValues {

  "Path" should "return correct pos" in {
    val first = Coordinate(0, 0)
    val second = Coordinate(1, 1)
    val p = first.path(second)

    val res = p.probe(0)
    val expected = first
    res should be(expected)
  }

  it should "handle max indices" in {
    val first = Coordinate(0, 0)
    val second = Coordinate(1, 1)
    val p = first.path(second)
    val outOfBound = 5

    val res = p.probe(outOfBound)
    val expected = second
    res should be(expected)
  }

  it should "handle negative indices" in {
    val first = Coordinate(0, 0)
    val second = Coordinate(1, 1)
    val p = first.path(second)
    val negative = -2

    val res = p.probe(negative)
    val expected = first
    res should be(expected)
  }
}
