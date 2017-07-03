package ru.agny.xent.battle

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.battle.unit.{Backpack, Troop}
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.unit.equip.Equipment
import ru.agny.xent.core.unit.{Level, Spirit, Soul}
import ru.agny.xent.core.utils.{TimeUnit, NESeq}

class MilitaryTest extends FlatSpec with Matchers with EitherValues {

  "Military tick" should "create battles if needed" in {
    val start = {
      val userOne = 1
      val userTwo = 2
      val dummySoul = Soul(1, Level(1, 1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val toughSoul = Soul(2, Level(1, 1, 1), Spirit(10, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, Coordinate(1, 1))
      val troopTwo = Troop(2, NESeq(toughSoul +: Vector.empty), Backpack.empty, userTwo, Coordinate(2, 2))
      val attacking = Vector(troopOne).map(x => x -> Movement(x.pos, Coordinate(2, 2)))
      val waiting = Vector(troopTwo).map(x => x -> new Waiting(x.pos))
      Military(attacking ++ waiting)
    }
    val (result, out) = start.tick(System.currentTimeMillis() + TimeUnit.minute * 30)
    result.troops.size should be(1)
    out.size should be(1)
  }

  "Military tick" should "join troops to battles if needed" in {
    val start = {
      val userOne = 1
      val userTwo = 2
      val dummySoul = Soul(1, Level(1, 1, 1), Spirit(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val toughSoul = Soul(2, Level(1, 1, 1), Spirit(10, 1, 1), Equipment.empty, 10, Vector.empty)
      val quickSoul = Soul(3, Level(1, 1, 1), Spirit(1, 1, 1), Equipment.empty, 15, Vector.empty)
      val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, Coordinate(1, 1))
      val troopTwo = Troop(2, NESeq(toughSoul +: Vector.empty), Backpack.empty, userTwo, Coordinate(2, 2))
      val troopThree = Troop(3, NESeq(quickSoul +: Vector.empty), Backpack.empty, userOne, Coordinate(3, 3))
      val attacking = Vector(troopOne, troopThree).map(x => x -> Movement(x.pos, Coordinate(2, 2)))
      val waiting = Vector(troopTwo).map(x => x -> new Waiting(x.pos))
      Military(attacking ++ waiting)
    }
    val firstBattleStart = System.currentTimeMillis() + TimeUnit.minute * 8
    val secondTroopJoinBattle = firstBattleStart + TimeUnit.minute * 4
    val twoRoundFightEnds = firstBattleStart + Round.timeLimitMax * 2
    val (firstEncounter, _) = start.tick(firstBattleStart)
    val (secondEncounter, _) = start.tick(secondTroopJoinBattle)
    val (result, out) = start.tick(twoRoundFightEnds)
    firstEncounter.troops.count { case (t, o: Battle) => true; case _ => false } should be(2)
    secondEncounter.troops.count { case (t, o: Battle) => true; case _ => false } should be(3)
    result.troops.size should be(1)
    out.size should be(2)
  }

}
