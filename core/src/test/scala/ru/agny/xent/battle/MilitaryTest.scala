package ru.agny.xent.battle

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.UserType._
import ru.agny.xent.battle.unit.{Backpack, Troop}
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.unit.characteristic.{PresencePower, Agility}
import ru.agny.xent.core.unit.equip.{StatProperty, Equipment}
import ru.agny.xent.core.unit._
import ru.agny.xent.core.utils.{TimeUnit, NESeq}

class MilitaryTest extends FlatSpec with Matchers with EitherValues {

  val userOne = 1L
  val userTwo = 2L
  val dummySoul = Soul(1, SoulData(Level(1, 1), Spirit(1, 1, 1), Stats.default, Vector.empty), Equipment.empty)
  val toughSoul = Soul(2, SoulData(Level(1, 1), Spirit(10, 1, 10), Stats(Vector(StatProperty(PresencePower, Level(5, 0)))), Vector.empty), Equipment.empty)
  val pos1 = Coordinate(1, 1)
  val pos2 = Coordinate(2, 2)
  val pos3 = Coordinate(3, 3)

  "Military tick" should "create battles if needed" in {
    val start = {
      val move = MovementPlan(Vector(Movement(pos1, pos2)), pos1)
      val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, move)
      val troopTwo = Troop(2, NESeq(toughSoul +: Vector.empty), Backpack.empty, userTwo, MovementPlan.idle(pos2))
      Military(Vector(troopOne, troopTwo), Vector.empty)
    }
    val (result, out) = start.tick(System.currentTimeMillis() + TimeUnit.minute * 30)
    result.troops.size should be(1)
    out.size should be(1)
  }

  "Military tick" should "join troops to battles if needed" in {
    val start = {
      val quickStats = Stats(Vector(StatProperty(Agility, Level(10, 0))))
      val quickSoul = Soul(3, SoulData(Level(1, 0), Spirit(1, 0, 1), quickStats, Vector.empty), Equipment.empty)
      val move1 = MovementPlan(Vector(Movement(pos1, pos2)), pos1)
      val move3 = MovementPlan(Vector(Movement(pos3, pos2)), pos3)
      val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, move1)
      val troopTwo = Troop(2, NESeq(toughSoul +: Vector.empty), Backpack.empty, userTwo, MovementPlan.idle(pos2))
      val troopThree = Troop(3, NESeq(quickSoul +: Vector.empty), Backpack.empty, userOne, move3)
      Military(Vector(troopOne, troopTwo, troopThree), Vector.empty)
    }
    val firstBattleStart = System.currentTimeMillis() + TimeUnit.minute * 8
    val secondTroopJoinBattle = firstBattleStart + TimeUnit.minute * 4
    val twoRoundFightEnds = firstBattleStart + Round.timeLimitMax * 2
    val (firstEncounter, _) = start.tick(firstBattleStart)
    val (secondEncounter, _) = start.tick(secondTroopJoinBattle)
    val (result, out) = start.tick(twoRoundFightEnds)
    firstEncounter.events.count { case _: Battle => true; case _ => false } should be(2)
    secondEncounter.events.count { case _: Battle => true; case _ => false } should be(3)
    result.troops.size should be(1)
    out.size should be(2)
  }

}
