package ru.agny.xent.core

import org.scalatest.{BeforeAndAfterAll, EitherValues, FlatSpec, Matchers}
import ru.agny.xent.TestHelper
import ru.agny.xent.battle.unit.{Backpack, StubArmor, Troop}
import ru.agny.xent.battle.{Movement, MovementPlan, Waiting}
import ru.agny.xent.core.city.Shape.FourShape
import ru.agny.xent.core.city._
import ru.agny.xent.core.inventory._
import ru.agny.xent.core.unit.equip.Equipment
import ru.agny.xent.core.utils.NESeq

class UserTest extends FlatSpec with Matchers with EitherValues with BeforeAndAfterAll {

  import TestHelper._

  val shape = FourShape.name
  val waitingCoordinate = new Waiting(Coordinate(0, 0), 0)
  val woodId = 1
  val soulOne = defaultSoul(1)
  val soulTwo = defaultSoul(2)
  val user = defaultUser()

  "User" should "create troop from the souls" in {
    val soul1 = (soulOne, waitingCoordinate)
    val soul2 = (soulTwo, waitingCoordinate)
    val souls = Workers(Vector(soul1, soul2))
    val userWithSouls = user.copy(souls = souls)
    val (soulless, troop) = userWithSouls.createTroop(3, Vector(1, 2)).right.value
    soulless.souls should be(Workers.empty)
    troop.activeUnits should be(Vector(soul1._1, soul2._1))
  }

  it should "not take occupied souls to the troop" in {
    val soul1 = (soulOne, waitingCoordinate)
    val soul2 = (soulTwo, Movement(Coordinate(0, 0), Coordinate(1, 2)))
    val souls = Workers(Vector(soul1, soul2))
    val userWithSouls = user.copy(souls = souls)
    val (userWithSoul, troop) = userWithSouls.createTroop(3, Vector(1, 2)).right.value
    userWithSoul.souls should be(Workers(Vector(soul2)))
    troop.activeUnits should be(Vector(soul1._1))
  }

  it should "add production to facility queue" in {
    val toProduce = Producible(1, "meh", ProductionSchema(1000, Cost(Vector.empty), Set.empty))
    val building = Building(Coordinate(2, 2), "Test", Vector(toProduce), 1000).finish
    val facilityId = building.id
    val city = City.empty(0, 0).place(building, Shape.values(shape).form(building.c))
    val user = User(1, "Vasya", city.right.value)
    val res = user.addProduction(facilityId, ItemStack(4, 1))
    val queue = res.right.value.city.producers.find(_.id == facilityId).get.queue
    queue.content should be(Vector((toProduce, 4)))
  }

  it should "consume troop's lifepower and get loot" in {
    val armor = StubArmor()
    val loot = ItemStack(10, woodId)
    val armoredSoul = soulOne.copy(equip = Equipment.empty.add(armor)._1)
    val souls = Vector(armoredSoul, soulTwo)
    val soulsLifePower = souls.foldLeft(0)((acc, x) => acc + x.beAssimilated()._1)
    val idleAtCity = MovementPlan(Vector(new Waiting(user.city.c)), user.city.c)
    val troop = Troop(1, NESeq(souls), Backpack.empty.add(Vector(loot))._1, user.id, idleAtCity)
    val userWithTroop = user.assimilateTroop(troop)

    userWithTroop.power should be(user.power.regain(soulsLifePower, soulsLifePower / 10))
    userWithTroop.city.storage.getItem(armor.id) should be(Some(armor))
    userWithTroop.city.storage.getItem(woodId) should be(Some(loot))
  }

}
