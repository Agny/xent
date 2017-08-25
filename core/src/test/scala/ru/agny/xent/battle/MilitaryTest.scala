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
  val dummySoul = Soul(1, SoulData(Level(1, 1), Spirit(1, 1, 1), Stats(Vector.empty), Vector.empty), Equipment.empty)
  val toughSoul = Soul(2, SoulData(Level(1, 1), Spirit(10, 1, 10), Stats(Vector(StatProperty(PresencePower, Level(5, 0)))), Vector.empty), Equipment.empty)
  val pos1 = Coordinate(1, 1)
  val pos2 = Coordinate(2, 2)
  val pos3 = Coordinate(2, 3)

  "Military tick" should "create battles if needed" in {
    val start = {
      val move = MovementPlan(Vector(Movement(pos1, pos2)), pos1)
      val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, move)
      val troopTwo = Troop(2, NESeq(toughSoul +: Vector.empty), Backpack.empty, userTwo, MovementPlan.idle(pos2))
      Military(Vector(troopOne, troopTwo), Vector.empty, System.currentTimeMillis())
    }
    val timeToBattleAndBack = (Distance.tileToDistance(2) * 2) / Speed.default
    val enoughTime = System.currentTimeMillis() + timeToBattleAndBack + Round.timeLimitMax
    val (result, out) = start.tick(enoughTime)
    result.troops.size should be(1)
    out.size should be(1)
  }

  it should "join troops to battles if needed" in {
    val start = {
      val move1 = MovementPlan(Vector(Movement(pos1, pos2)), pos1)
      val move3 = MovementPlan(Vector(Movement(pos3, pos2)), pos3)
      val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, move1)
      val troopTwo = Troop(2, NESeq(toughSoul +: Vector.empty), Backpack.empty, userTwo, MovementPlan.idle(pos2))
      val troopThree = Troop(3, NESeq(getQuickSoul(3) +: Vector.empty), Backpack.empty, userOne, move3)
      Military(Vector(troopOne, troopTwo, troopThree), Vector.empty, System.currentTimeMillis())
    }
    val firstBattleStart = System.currentTimeMillis() + TimeUnit.minute * 8
    val secondTroopJoinBattle = firstBattleStart + TimeUnit.minute * 4
    val twoRoundFightEnds = secondTroopJoinBattle + Round.timeLimitMax * 2
    val (firstEncounter, lfirst) = start.tick(firstBattleStart)
    val (secondEncounter, lsecond) = firstEncounter.tick(secondTroopJoinBattle)
    val (result, out) = secondEncounter.tick(twoRoundFightEnds)
    firstEncounter.events.collect { case b: Battle => b.troops.size }.head should be(2)
    secondEncounter.events.collect { case b: Battle => b.troops.size }.head should be(2)
    result.troops.size should be(1)
    out.size should be(2)
  }

  it should "send troops back after loosing all souls" in {
    val quickSoul = getQuickSoul(3)
    val start = {
      val move = MovementPlan(Vector(Movement(pos1, pos3)), pos1)
      val troopOne = Troop(1, NESeq(quickSoul +: Vector.empty), Backpack.empty, userOne, move)
      val troopTwo = Troop(2, NESeq(toughSoul +: Vector.empty), Backpack.empty, userTwo, MovementPlan.idle(pos2))
      Military(Vector(troopOne, troopTwo), Vector.empty, System.currentTimeMillis())
    }
    val battleStart = System.currentTimeMillis() + Distance.tileToDistance(2) / quickSoul.speed
    val (withEncounter, _) = start.tick(battleStart)
    val battleEnd = battleStart + withEncounter.events.head.asInstanceOf[Battle].round.duration
    val (moved, _) = withEncounter.tick(battleEnd)
    val (result, out) = moved.tick(battleEnd + Distance.tileToDistance(2) / Speed.default)

    out.head.move(0) should be(Coordinate(1, 1))
  }

  it should "continue troops movement after battle" in {
    val quickSoulSample = getQuickSoul(0)
    val start = {
      val move = MovementPlan(Vector(Movement(pos1, pos3)), pos1)
      val troopOne = Troop(1, NESeq(Vector(getQuickSoul(3), getQuickSoul(4), getQuickSoul(5))), Backpack.empty, userOne, move)
      val troopTwo = Troop(2, NESeq(toughSoul +: Vector.empty), Backpack.empty, userTwo, MovementPlan.idle(pos2))
      Military(Vector(troopOne, troopTwo), Vector.empty, System.currentTimeMillis())
    }
    val battleStart = System.currentTimeMillis() + Distance.tileToDistance(2) / quickSoulSample.speed
    val (firstRound, _) = start.tick(battleStart)
    val firstRoundEnd = battleStart + firstRound.events.head.asInstanceOf[Battle].round.duration
    val (result, _) = firstRound.tick(firstRoundEnd + Distance.tileToDistance(1) / quickSoulSample.speed)

    result.troops.last.move(0) should be(pos3)
  }

  def getQuickSoul(id: ObjectId) = {
    val quickStats = Stats(Vector(StatProperty(Agility, Level(10, 0))))
    Soul(id, SoulData(Level(1, 0), Spirit(1, 0, 1), quickStats, Vector.empty), Equipment.empty)
  }

}
