package ru.agny.xent.realm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.{Args, Status}
import ru.agny.xent.realm.Hexagon

import scala.language.implicitConversions

class PathTest extends AnyFlatSpec {

  "Path" should "be correct" in {
    val start = Hexagon(0, 0)
    val destination = Hexagon(1, 1)
    val p = start.path(destination)

    val c1 = p.placeCursor(0)
    val c2 = p.next()
    val c3 = p.next()

    c1 should be(start)
    c2 should be(Hexagon(0, 1)) // it's algorythm-dependent
    c3 should be(destination)
    p.next() should be(destination)
  }

  it should "handle max indices" in {
    val start = Hexagon(0, 0)
    val destination = Hexagon(1, 1)
    val p = start.path(destination)
    val outOfBound = 5

    val res = p.placeCursor(outOfBound)
    val expected = destination
    res should be(expected)
  }

  it should "handle negative indices" in {
    val start = Hexagon(0, 0)
    val destination = Hexagon(1, 1)
    val p = start.path(destination)
    val negative = -2

    val res = p.placeCursor(negative)
    val expected = start
    res should be(expected)
  }
}
