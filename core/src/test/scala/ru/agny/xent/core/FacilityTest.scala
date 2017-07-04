package ru.agny.xent.core

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.TestHelper
import ru.agny.xent.core.unit.equip.Equipment
import ru.agny.xent.core.unit.{Level, Spirit, Soul}
import ru.agny.xent.core.utils.TimeUnit

class FacilityTest extends FlatSpec with Matchers with EitherValues {

  import TestHelper._
  import Item.implicits._

  val worker = defaultSoul(1)
  val woodId = 1
  val prodId = 2
  val copperId = 3

  "Building" should "assign worker" in {
    val res = Producible(prodId, "Test res", ProductionSchema(1000, Cost(Vector(ItemStack(5, woodId))), Set.empty))
    val (facility, _) = Building("test", Vector(res), 0).finish.run(worker)
    facility.worker should be(Some(worker))
  }

  it should "replace worker" in {
    val replacement = defaultSoul(2)
    val res = Producible(prodId, "Test res", ProductionSchema(1000, Cost(Vector(ItemStack(5, woodId))), Set.empty))
    val (facility, _) = Building("test", Vector(res), 0).finish.run(worker)
    val (facilityUpdated, ex) = facility.run(replacement)
    facilityUpdated.worker should be(Some(replacement))
    ex should be(Some(worker))
  }

  it should "produce resource in queue" in {
    val res = Producible(prodId, "Test res", ProductionSchema(1000, Cost(Vector(ItemStack(5, woodId))), Set.empty))
    val (facility, _) = Building("test", Vector(res), 0).finish.run(worker)
    val storage = Storage(Vector(ItemStack(10, woodId)))
    val (sAfterSpend, updatedFacilityQueue) = facility.addToQueue(ItemStack(1, res.id))(storage).right.value
    val (result, _) = sAfterSpend.tick(1000, Vector(updatedFacilityQueue))
    val expected = Vector(ItemStack(1, res.id), ItemStack(5, woodId))
    result.resources should be(expected)
  }

  it should "stop production" in {
    val res = Producible(prodId, "Test res", ProductionSchema(1000, Cost(Vector(ItemStack(5, woodId))), Set.empty))
    val (facility, _) = Building("test", Vector(res), 0).finish.run(worker)
    val storage = Storage(Vector(ItemStack(10, woodId)))
    val (sAfterSpend, updatedFacilityQueue) = facility.addToQueue(ItemStack(2, res.id))(storage).right.value
    val (sWithProduced, producers) = sAfterSpend.tick(1000, Vector(updatedFacilityQueue))

    val (stopped, exworker) = producers.head.stop
    val (sWithStopped, stoppedUnchanged) = sWithProduced.tick(2000, Vector(stopped))

    sWithProduced.resources should be(Vector(ItemStack(1, res.id)))
    stopped.queue.isEmpty should be(false)
    sWithStopped should be theSameInstanceAs sWithProduced
    stopped.queue.progress should be(stoppedUnchanged.head.queue.progress)
    exworker should be(Some(worker))
  }


  "Outpost" should "produce resource in time" in {
    val res = Extractable(copperId, " Copper", 30, 1000, Set.empty)
    val (facility, _) = Outpost("Copper mine", res, Vector.empty, 10000).finish.run(worker)
    val storage = Storage.empty
    val (s, _) = storage.tick(TimeUnit.minute, Vector(facility))

    s.resources should be(Vector(ItemStack(30, copperId)))
    res.volume should be(0)
    facility.queue.isEmpty should be(true)
  }

  it should "handle gaps in time" in {
    val res = Extractable(copperId, " Copper", 30, 1000, Set.empty)
    val (facility, _) = Outpost("Copper mine", res, Vector.empty, 10000).finish.run(worker)
    val (s, f) = Storage.empty.tick(TimeUnit.minute / 6, Vector(facility))
    val (stopped, _) = f.head.stop
    val (sameS, sameStopped) = s.tick(TimeUnit.minute, Vector(stopped))
    val (run, _) = sameStopped.head.run(worker)
    val (result, _) = sameS.tick(TimeUnit.minute / 6, Vector(run))

    s.resources should be(Vector(ItemStack(10, copperId)))
    result.resources should be(Vector(ItemStack(20, copperId)))
    res.volume should be (10)
    facility.queue.isEmpty should be(false)
  }

}
