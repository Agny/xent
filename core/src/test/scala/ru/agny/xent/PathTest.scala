package ru.agny.xent

import org.scalactic.source.Position
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.{Args, Status}
import ru.agny.xent.realm.Coordinate

import scala.language.implicitConversions

class PathTest extends AnyFlatSpec {

  "Path" should "be correct" in {
    val start = Coordinate(0, 0)
    val destination = Coordinate(1, 1)
    val p = start.path(destination)

    val c1 = p.probe(0)
    val c2 = p.probe(1)
    val c3 = p.probe(2)

    c1 should be(start)
    c2 should be(Coordinate(0, 1)) // it's algorythm-dependent
    c3 should be(destination)
    p.tiles should be(2) // 0:1->1:1
  }

  it should "handle max indices" in {
    val start = Coordinate(0, 0)
    val destination = Coordinate(1, 1)
    val p = start.path(destination)
    val outOfBound = 5

    val res = p.probe(outOfBound)
    val expected = destination
    res should be(expected)
  }

  it should "handle negative indices" in {
    val start = Coordinate(0, 0)
    val destination = Coordinate(1, 1)
    val p = start.path(destination)
    val negative = -2

    val res = p.probe(negative)
    val expected = start
    res should be(expected)
  }
}
