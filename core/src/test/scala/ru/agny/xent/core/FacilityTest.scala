package ru.agny.xent.core

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.ResourceUnit

class FacilityTest extends FlatSpec with Matchers with EitherValues {
  val shape = FourShape(LocalCell(1,1))

  "Building" should "produce resource in queue" in {
    val res = Producible("Test res", Seq(ResourceUnit(5, "Wood")), 1000, Set.empty)
    val facility = Building("test", Seq(res), 0, shape)
    val storage = Storage(Seq(ResourceUnit(10, "Wood")), Seq(facility))
    val afterSpend = facility.addToQueue(ResourceUnit(1, res.name))(storage)
    val result = afterSpend.right.value.tick(System.currentTimeMillis() - 1000)
    val expected = Seq(ResourceUnit(1, res.name), ResourceUnit(5, "Wood"))
    result.resources should be(expected)
    result.producers.head should be (facility)
  }

}
