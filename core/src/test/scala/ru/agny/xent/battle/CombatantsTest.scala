package ru.agny.xent.battle

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.battle.unit.{Backpack, Troop}
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.unit.equip.Equipment
import ru.agny.xent.core.unit.{Level, Spirit, Soul}
import ru.agny.xent.core.utils.NESeq

class CombatantsTest extends FlatSpec with Matchers with EitherValues {

  val pos = Coordinate(1, 1)
  val defaultOccupation = new Waiting(pos, System.currentTimeMillis())

  "Combatants" should "add troops to the queue" in {
    val (start, added) = {
      val dummySoul = Soul(1, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, 1, pos) -> defaultOccupation
      val toQueue = Vector(Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, 1, pos) -> defaultOccupation)
      (Combatants(NESeq(troopOne +: Vector.empty), Vector.empty), toQueue)
    }

    val result = start.queue(added)
    result.queue should be(added)
  }

  it should "group troops by user and map them to self ids" in {
    val (start, expected) = {
      val userOne = 1
      val userTwo = 2
      val dummySoul = Soul(1, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(dummySoul +: Vector.empty), Backpack.empty, userTwo, pos)
      val troopThree = Troop(3, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, pos)
      val troopQueue = Troop(4, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, pos)
      val troops = Vector(troopOne, troopTwo, troopThree).map(_ -> defaultOccupation)
      val start = Combatants(NESeq(troops), Vector(troopQueue -> defaultOccupation))
      (start, Map(
        userOne -> Map(1 -> troopOne, 3 -> troopThree),
        userTwo -> Map(2 -> troopTwo)
      ))
    }

    val result = start.groupByUsers
    result should contain theSameElementsAs expected
  }

  it should "test troops if the battle must go on" in {
    val start = {
      val userOne = 1
      val soulOne = Soul(userOne, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val userTwo = 2
      val soulTwo = Soul(userTwo, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulTwo +: Vector.empty), Backpack.empty, userTwo, pos)
      Vector(troopOne, troopTwo)
    }

    Combatants.isBattleNeeded(start) should be(true)
  }

  it should "test troops if the battle must go on with queued troops" in {
    val (t, q) = {
      val userOne = 1
      val soulOne = Soul(userOne, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val userTwo = 2
      val soulTwo = Soul(userTwo, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val userThree = 3
      val soulThree = Soul(userThree, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulTwo +: Vector.empty), Backpack.empty, userTwo, pos)
      val troopQueue = Vector(Troop(3, NESeq(soulThree +: Vector.empty), Backpack.empty, userThree, pos))
      val troops = Vector(troopOne, troopTwo)
      (troops, troopQueue)
    }

    Combatants.isBattleNeeded(t ++ q) should be(true)
  }

  it should "test troops if the battle must end" in {
    val dummySoul = Soul(1, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
    val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, 1, pos)
    Combatants.isBattleNeeded(Vector(troopOne)) should be(false)
  }

  it should "test troops if the battle must end with queued troops" in {
    val (t, q) = {
      val userOne = 1
      val soulOne = Soul(userOne, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulTwo = Soul(userOne, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulThree = Soul(userOne, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulTwo +: Vector.empty), Backpack.empty, userOne, pos)
      val troopQueue = Vector(Troop(3, NESeq(soulThree +: Vector.empty), Backpack.empty, userOne, pos))
      val troops = Vector(troopOne, troopTwo)
      (troops, troopQueue)
    }

    Combatants.isBattleNeeded(t ++ q) should be(false)
  }

  it should "free all troops" in {
    val (start, expected) = {
      val userOne = 1
      val soulOne = Soul(userOne, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulTwo = Soul(userOne, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulThree = Soul(userOne, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulTwo +: Vector.empty), Backpack.empty, userOne, pos)
      val troopQueue = Troop(3, NESeq(soulThree +: Vector.empty), Backpack.empty, userOne, pos)
      val troops = Vector(troopOne, troopTwo).map(_ -> defaultOccupation)
      val queue = Vector(troopQueue).map(_ -> defaultOccupation)
      (Combatants(NESeq(troops), queue), troops ++ queue)
    }
    start.free should be(expected)
  }

  it should "carry only combat-able troops to the next round" in {
    val (start, toNext, out) = {
      val userOne = 1
      val userTwo = 2
      val userThree = 3
      val soulOne = Soul(1, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulTwo = Soul(2, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulThree = Soul(3, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)

      val fallen = Soul(1, Level(1, 1), Spirit(0, 1, 1), Equipment.empty, 10, Vector.empty)
      val fallenTwo = Soul(2, Level(1, 1), Spirit(0, 1, 1), Equipment.empty, 10, Vector.empty)

      val troopOne = Troop(1, NESeq(Vector(soulOne, fallen)), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(Vector(soulTwo, fallenTwo)), Backpack.empty, userTwo, pos, Fatigue.MAX)
      val troopQueue = Troop(3, NESeq(soulThree +: Vector.empty), Backpack.empty, userThree, pos)
      val troops = Vector(troopOne, troopTwo).map(_ -> defaultOccupation)

      (Combatants(NESeq(troops), Vector(troopQueue -> defaultOccupation)),
        Vector(troopOne, troopQueue).map(_ -> defaultOccupation),
        Vector(troopTwo).map(_ -> defaultOccupation))
    }

    val (res, outRes) = Combatants.prepareToNextRound(start, start.troops.unzip._1.toVector)
    res.get.troops should be(toNext)
    outRes should be(out)
    res.get.queue should be(Vector.empty)
  }

  it should "adjust the pool of troops" in {
    val (pool, adjustment, expected) = {
      val userOne = 1L
      val userTwo = 2L
      val dummySoul = Soul(1, Level(1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(dummySoul +: Vector.empty), Backpack.empty, userTwo, pos, Fatigue.MAX)
      val troopThree = Troop(3, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, pos, Fatigue.MAX)
      val troopThreeRested = Troop(3, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, pos)
      (Map(userOne -> Map(troopOne.id -> troopOne, troopThree.id -> troopThree), userTwo -> Map(troopTwo.id -> troopTwo)),
        troopThreeRested,
        Map(userOne -> Map(troopOne.id -> troopOne, troopThree.id -> troopThreeRested), userTwo -> Map(troopTwo.id -> troopTwo))
        )
    }

    val res = Combatants.adjustPool(pool, adjustment)
    res should be(expected)
  }
}
