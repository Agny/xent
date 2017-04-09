package ru.agny.xent.core

import org.scalatest.{EitherValues, Matchers, FlatSpec}

class FacilityTest extends FlatSpec with Matchers with EitherValues {

  import Item.implicits._

  val shape = FourShape(LocalCell(1,1))
  val woodId = 1
  val prodId = 2

  "Building" should "produce resource in queue" in {
    val res = Producible(prodId, "Test res", Seq(ResourceUnit(5, woodId)), 1000, Set.empty)
    val facility = Building(1, "test", Seq(res), 0, shape)
    val storage = Storage(Seq(ResourceUnit(10, woodId)))
    val (sAfterSpend, updatedFacilityQueue) = facility.addToQueue(ResourceUnit(1, res.id))(storage).right.value
    val (result, _) = sAfterSpend.tick(System.currentTimeMillis() - 1000, Seq(updatedFacilityQueue))
    val expected = Seq(ResourceUnit(1, res.id), ResourceUnit(5, woodId))
    result.resources should be(expected)
  }

}
