package ru.agny.xent.core

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.core.utils.TimeUnit

class FacilityTest extends FlatSpec with Matchers with EitherValues {

  import Item.implicits._

  val shape = FourShape(LocalCell(1, 1))
  val woodId = 1
  val prodId = 2
  val copperId = 3

  "Building" should "produce resource in queue" in {
    val res = Producible(prodId, "Test res", ProductionSchema(1000, Cost(Vector(ItemStack(5, woodId))), Set.empty))
    val facility = Building(1, "test", Vector(res), 0, shape)
    val storage = Storage(Vector(ItemStack(10, woodId)))
    val (sAfterSpend, updatedFacilityQueue) = facility.addToQueue(ItemStack(1, res.id))(storage).right.value
    val (result, _) = sAfterSpend.tick(System.currentTimeMillis() - 1000, Vector(updatedFacilityQueue))
    val expected = Vector(ItemStack(1, res.id), ItemStack(5, woodId))
    result.resources should be(expected)
  }

  "Outpost" should "produce resource in time" in {
    val res = Extractable(copperId, " Copper", 30, 1000, Set.empty)
    val facility = Outpost(copperId, "Copper mine", res, Vector.empty, 10000)
    val storage = Storage.empty
    val (s, _) = facility.tick(TimeUnit.minute)(storage)

    s.resources should be(Vector(ItemStack(30, copperId)))
    res.volume should be(0)
    facility.main.volume should be(0)
  }

}
