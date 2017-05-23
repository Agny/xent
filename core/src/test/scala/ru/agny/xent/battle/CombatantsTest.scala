package ru.agny.xent.battle

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.battle.core.LevelBar
import ru.agny.xent.battle.unit.{Fatigue, SpiritBar, Soul, Troop}
import ru.agny.xent.battle.unit.inventory.{Equipment, Backpack}
import ru.agny.xent.core.Coordinate

class CombatantsTest extends FlatSpec with Matchers with EitherValues {

  val pos = Coordinate(1, 1)
  val defaultOccupation = new Waiting(pos, System.currentTimeMillis())

  "Combatants" should "add troops to the queue" in {
    val start = Combatants(Vector.empty, Vector.empty)
    val troop = Troop(1, Vector.empty, Backpack.empty, 1, pos)
    val added = Vector(troop -> defaultOccupation)
    val result = start.queue(added)
    result.queue should be(added)
  }

  it should "group troops by user and map them to self ids" in {
    val (start, expected) = {
      val userOne = 1
      val userTwo = 2
      val troopOne = Troop(1, Vector.empty, Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, Vector.empty, Backpack.empty, userTwo, pos)
      val troopThree = Troop(3, Vector.empty, Backpack.empty, userOne, pos)
      val troopQueue = Troop(4, Vector.empty, Backpack.empty, userOne, pos)
      val troops = Vector(troopOne, troopTwo, troopThree).map(_ -> defaultOccupation)
      val start = Combatants(troops, Vector(troopQueue -> defaultOccupation))
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
      val soulOne = Soul(userOne, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val userTwo = 2
      val soulTwo = Soul(userTwo, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, Vector(soulOne), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, Vector(soulTwo), Backpack.empty, userTwo, pos)
      val troops = Vector(troopOne, troopTwo).map(_ -> defaultOccupation)
      Combatants(troops, Vector.empty)
    }

    start.isBattleNeeded should be(true)
  }

  it should "test troops if the battle must go on with queued troops" in {
    val start = {
      val userOne = 1
      val soulOne = Soul(userOne, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val userTwo = 2
      val soulTwo = Soul(userTwo, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val userThree = 3
      val soulThree = Soul(userThree, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, Vector(soulOne), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, Vector(soulTwo), Backpack.empty, userTwo, pos)
      val troopQueue = Troop(3, Vector(soulThree), Backpack.empty, userThree, pos)
      val troops = Vector(troopOne, troopTwo).map(_ -> defaultOccupation)
      Combatants(troops, Vector(troopQueue -> defaultOccupation))
    }

    start.isBattleNeeded should be(true)
  }

  it should "test troops if the battle must end" in {
    val start = Combatants(Vector.empty, Vector.empty)
    start.isBattleNeeded should be(false)
  }

  it should "test troops if the battle must end with queued troops" in {
    val start = {
      val userOne = 1
      val soulOne = Soul(userOne, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulTwo = Soul(userOne, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulThree = Soul(userOne, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, Vector(soulOne), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, Vector(soulTwo), Backpack.empty, userOne, pos)
      val troopQueue = Troop(3, Vector(soulThree), Backpack.empty, userOne, pos)
      val troops = Vector(troopOne, troopTwo).map(_ -> defaultOccupation)
      Combatants(troops, Vector(troopQueue -> defaultOccupation))
    }

    start.isBattleNeeded should be(false)
  }

  it should "free all troops" in {
    val (start, expected) = {
      val userOne = 1
      val soulOne = Soul(userOne, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulTwo = Soul(userOne, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulThree = Soul(userOne, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, Vector(soulOne), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, Vector(soulTwo), Backpack.empty, userOne, pos)
      val troopQueue = Troop(3, Vector(soulThree), Backpack.empty, userOne, pos)
      val troops = Vector(troopOne, troopTwo).map(_ -> defaultOccupation)
      val queue = Vector(troopQueue).map(_ -> defaultOccupation)
      (Combatants(troops, queue), troops ++ queue)
    }
    start.free should be(expected)
  }

  it should "carry only combat-able troops to the next round" in {
    val (start, toNext, out) = {
      val userOne = 1
      val userTwo = 2
      val userThree = 3
      val soulOne = Soul(userOne, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulTwo = Soul(userTwo, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulThree = Soul(userThree, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)

      val fallen = Soul(userOne, LevelBar(1, 1, 1), SpiritBar(0, 1, 1), Equipment.empty, 10, Vector.empty)
      val fallenTwo = Soul(userTwo, LevelBar(1, 1, 1), SpiritBar(0, 1, 1), Equipment.empty, 10, Vector.empty)

      val troopOne = Troop(1, Vector(soulOne, fallen), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, Vector(soulTwo, fallenTwo), Backpack.empty, userTwo, pos, Fatigue.MAX)
      val troopQueue = Troop(3, Vector(soulThree), Backpack.empty, userThree, pos)
      val troops = Vector(troopOne, troopTwo).map(_ -> defaultOccupation)

      (Combatants(troops, Vector(troopQueue -> defaultOccupation)),
        Vector(troopOne, troopQueue).map(_ -> defaultOccupation),
        Vector(troopTwo).map(_ -> defaultOccupation))
    }

    val (res, outRes) = Combatants.nextRound(start, start.troops.unzip._1)
    res.troops should be(toNext)
    outRes should be(out)
    res.queue should be(Vector.empty)
  }

  it should "adjust the pool of troops" in {
    val (pool, adjustment, expected) = {
      val userOne = 1L
      val userTwo = 2L
      val troopOne = Troop(1, Vector.empty, Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, Vector.empty, Backpack.empty, userTwo, pos, Fatigue.MAX)
      val troopThree = Troop(3, Vector.empty, Backpack.empty, userOne, pos, Fatigue.MAX)
      val troopThreeRested = Troop(3, Vector.empty, Backpack.empty, userOne, pos)
      (Map(userOne -> Map(troopOne.id -> troopOne, troopThree.id -> troopThree), userTwo -> Map(troopTwo.id -> troopTwo)),
        troopThreeRested,
        Map(userOne -> Map(troopOne.id -> troopOne, troopThree.id -> troopThreeRested), userTwo -> Map(troopTwo.id -> troopTwo))
        )
    }

    val res = Combatants.adjustPool(pool, adjustment)
    res should be(expected)
  }
}
