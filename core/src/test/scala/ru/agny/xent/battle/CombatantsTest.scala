package ru.agny.xent.battle

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.TestHelper
import ru.agny.xent.battle.unit.{Backpack, Troop}
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.unit.equip.Equipment
import ru.agny.xent.core.unit._
import ru.agny.xent.core.utils.NESeq

class CombatantsTest extends FlatSpec with Matchers with EitherValues {

  import TestHelper._

  val pos = MovementPlan.idle(Coordinate(1, 1))
  val userOne = 1L
  val userTwo = 2L
  val soulOne = defaultSoul(1)
  val soulTwo = defaultSoul(2)

  "Combatants" should "add troops to the queue" in {
    val (start, added) = {
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, 1, pos)
      val toQueue = Vector(Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, 1, pos))
      (Combatants(NESeq(troopOne +: Vector.empty), Vector.empty), toQueue)
    }

    val result = start.queue(added)
    result.queue should be(added)
  }

  it should "group troops by user and map them to self ids" in {
    val (start, expected) = {
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulOne +: Vector.empty), Backpack.empty, userTwo, pos)
      val troopThree = Troop(3, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopQueue = Troop(4, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troops = Vector(troopOne, troopTwo, troopThree)
      val start = Combatants(NESeq(troops), Vector(troopQueue))
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
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulTwo +: Vector.empty), Backpack.empty, userTwo, pos)
      Vector(troopOne, troopTwo)
    }

    Combatants.isBattleNeeded(start) should be(true)
  }

  it should "test troops if the battle must go on with queued troops" in {
    val (t, q) = {
      val soulThree = defaultSoul(3)
      val userThree = 3
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulTwo +: Vector.empty), Backpack.empty, userTwo, pos)
      val troopQueue = Vector(Troop(3, NESeq(soulThree +: Vector.empty), Backpack.empty, userThree, pos))
      val troops = Vector(troopOne, troopTwo)
      (troops, troopQueue)
    }

    Combatants.isBattleNeeded(t ++ q) should be(true)
  }

  it should "test troops if the battle must end" in {
    val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, 1, pos)
    Combatants.isBattleNeeded(Vector(troopOne)) should be(false)
  }

  it should "test troops if the battle must end with queued troops" in {
    val (t, q) = {
      val soulThree = defaultSoul(3)
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
      val soulThree = defaultSoul(3)
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulTwo +: Vector.empty), Backpack.empty, userOne, pos)
      val troopQueue = Troop(3, NESeq(soulThree +: Vector.empty), Backpack.empty, userOne, pos)
      val troops = Vector(troopOne, troopTwo)
      val queue = Vector(troopQueue)
      (Combatants(NESeq(troops), queue), troops ++ queue)
    }
    start.free should be(expected)
  }

  it should "carry only combat-able troops to the next round" in {
    val (start, toNext, out) = {
      val soulThree = defaultSoul(3)

      val fallen = Soul(4, SoulData(Level(1, 1), Spirit(0, 1, 1), Stats.default, Vector.empty), Equipment.empty)
      val fallenTwo = Soul(5, SoulData(Level(1, 1), Spirit(0, 1, 1), Stats.default, Vector.empty), Equipment.empty)

      val userThree = 3
      val troopOne = Troop(1, NESeq(Vector(soulOne, fallen)), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(Vector(soulTwo, fallenTwo)), Backpack.empty, userTwo, pos, Fatigue.MAX)
      val troopQueue = Troop(3, NESeq(soulThree +: Vector.empty), Backpack.empty, userThree, pos)
      val troops = Vector(troopOne, troopTwo)

      (Combatants(NESeq(troops), Vector(troopQueue)),
        Vector(troopOne, troopQueue),
        Vector(troopTwo))
    }

    val (res, outRes) = Combatants.prepareToNextRound(start, start.troops.toVector)
    res.get.troops should be(toNext)
    outRes should be(out)
    res.get.queue should be(Vector.empty)
  }

  it should "adjust the pool of troops" in {
    val (pool, adjustment, expected) = {
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulOne +: Vector.empty), Backpack.empty, userTwo, pos, Fatigue.MAX)
      val troopThree = Troop(3, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos, Fatigue.MAX)
      val troopThreeRested = Troop(3, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      (Map(userOne -> Map(troopOne.id -> troopOne, troopThree.id -> troopThree), userTwo -> Map(troopTwo.id -> troopTwo)),
        troopThreeRested,
        Map(userOne -> Map(troopOne.id -> troopOne, troopThree.id -> troopThreeRested), userTwo -> Map(troopTwo.id -> troopTwo))
        )
    }

    val res = Combatants.adjustPool(pool, adjustment)
    res should be(expected)
  }
}
