package ru.agny.xent.realm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.{Args, Status}
import ru.agny.xent.realm.Hexagon

import scala.language.implicitConversions

class HexagonTest extends AnyFlatSpec {

  "Hexagon" should "calculate distance" in {
    val from = Hexagon(0, 0)
    val to = Hexagon(3, 1)
    val res = from.distance(to)
    val expected = 4
    res should be(expected)
  }

  it should "calculate distance to itself as zero" in {
    val pos = Hexagon(0, 0)
    val res = pos.distance(pos)
    val expected = 0
    res should be(expected)
  }

  override def run(testName: Option[String], args: Args): Status = super.run(testName, args)

  protected override def runTest(testName: String, args: Args): Status = super.runTest(testName, args)
}
