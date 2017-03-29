package ru.agny.xent.core

import org.scalatest.{EitherValues, Matchers, FlatSpec}

class FacilityTest extends FlatSpec with Matchers with EitherValues {
  val shape = FourShape(LocalCell(1,1))
  val woodId = 1
  val prodId = 2

  "Building" should "produce resource in queue" in {
    val res = Producible(prodId, "Test res", Seq(ResourceUnit(5, woodId)), 1000, Set.empty)
    val facility = Building(1, "test", Seq(res), 0, shape)
    val storage = Storage(Seq(ResourceUnit(10, woodId)), Seq(facility))
    val afterSpend = facility.addToQueue(ResourceUnit(1, res.id))(storage)
    val result = afterSpend.right.value.tick(System.currentTimeMillis() - 1000)
    val expected = Seq(ResourceUnit(1, res.id), ResourceUnit(5, woodId))
    result.resources should be(expected)
    result.producers.head should be (facility)
  }

}
