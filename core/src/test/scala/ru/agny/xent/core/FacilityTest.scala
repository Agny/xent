package ru.agny.xent.core

import org.scalatest.{EitherValues, FlatSpec, Matchers}
import ru.agny.xent.TestHelper
import ru.agny.xent.core.city.{Building, Storage}
import ru.agny.xent.core.inventory._

class FacilityTest extends FlatSpec with Matchers with EitherValues {

  import TestHelper._
  import ru.agny.xent.core.inventory.Item.implicits._

  val worker = defaultSoul(1)
  val user = defaultUser()
  val place = Coordinate(0, 0)
  val woodId = 1
  val prodId = 2
  val copperId = 3

  "Building" should "assign worker" in {
    val res = Producible(prodId, "Test res", ProductionSchema(1000, Cost(Vector(ItemStack(5, woodId, defaultWeight))), defaultWeight, Set.empty))
    val (facility, _) = Building(place, "test", Vector(res), 0).finish.run(worker)
    facility.worker should be(Some(worker))
  }

  it should "replace worker" in {
    val replacement = defaultSoul(2)
    val res = Producible(prodId, "Test res", ProductionSchema(1000, Cost(Vector(ItemStack(5, woodId, defaultWeight))), defaultWeight, Set.empty))
    val (facility, _) = Building(place, "test", Vector(res), 0).finish.run(worker)
    val (facilityUpdated, ex) = facility.run(replacement)
    facilityUpdated.worker should be(Some(replacement))
    ex should be(Some(worker))
  }

  it should "produce resource in queue" in {
    val res = Producible(prodId, "Test res", ProductionSchema(1000, Cost(Vector(ItemStack(5, woodId, defaultWeight))), defaultWeight, Set.empty))
    val (facility, _) = Building(place, "test", Vector(res), 0).finish.run(worker)
    val storage = Storage(Vector(ItemStack(10, woodId, defaultWeight)))
    val (sAfterSpend, updatedFacilityQueue) = facility.addToQueue(ItemStack(1, res.id, defaultWeight))(storage).right.value
    val (result, _) = sAfterSpend.tick(1000, Vector(updatedFacilityQueue))
    val expected = Vector(ItemStack(1, res.id, defaultWeight), ItemStack(5, woodId, defaultWeight))
    result.items should be(expected)
  }

  it should "stop production" in {
    val res = Producible(prodId, "Test res", ProductionSchema(1000, Cost(Vector(ItemStack(5, woodId, defaultWeight))), defaultWeight, Set.empty))
    val (facility, _) = Building(place, "test", Vector(res), 0).finish.run(worker)
    val storage = Storage(Vector(ItemStack(10, woodId, defaultWeight)))
    val (sAfterSpend, updatedFacilityQueue) = facility.addToQueue(ItemStack(2, res.id, defaultWeight))(storage).right.value
    val (sWithProduced, producers) = sAfterSpend.tick(1000, Vector(updatedFacilityQueue))

    val (stopped, exworker) = producers.head.stop
    val (sWithStopped, stoppedUnchanged) = sWithProduced.tick(2000, Vector(stopped))

    sWithProduced.items should be(Vector(ItemStack(1, res.id, defaultWeight)))
    stopped.queue.isEmpty should be(false)
    sWithStopped should be theSameInstanceAs sWithProduced
    stopped.queue.progress should be(stoppedUnchanged.head.queue.progress)
    exworker should be(Some(worker))
  }
}
