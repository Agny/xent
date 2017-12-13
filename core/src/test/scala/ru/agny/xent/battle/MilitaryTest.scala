package ru.agny.xent.battle

import org.scalatest.{EitherValues, FlatSpec, Matchers}
import ru.agny.xent.core.utils.UserType._
import ru.agny.xent.battle.unit._
import ru.agny.xent.core.city.City
import ru.agny.xent.core.{Coordinate, User}
import ru.agny.xent.core.inventory.{Extractable, ItemStack}
import ru.agny.xent.core.unit.Characteristic.{Agility, PresencePower}
import ru.agny.xent.core.unit.equip.{Equipment, StatProperty}
import ru.agny.xent.core.unit._
import ru.agny.xent.core.utils.{NESeq, TimeUnit}

class MilitaryTest extends FlatSpec with Matchers with EitherValues {

  import ru.agny.xent.TestHelper._

  val userOne = 1L
  val userTwo = 2L
  val dummySoul = Soul(1, SoulData(Level(1, 1), 1, Stats(Vector.empty), Vector.empty), Equipment.empty)
  val toughSoul = Soul(2, SoulData(Level(1, 1), 10, Stats(Vector(StatProperty(PresencePower, Level(5, 0)))), Vector.empty), Equipment.empty)
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
    result.objects.size should be(1)
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
    result.objects.size should be(1)
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

    out.head.pos(0) should be(Coordinate(1, 1))
  }

  it should "continue troops movement after battle" in {
    val quickSoulSample = getQuickSoul(0)
    val move = MovementPlan(Vector(Movement(pos1, pos3)), pos1)
    val troopOne = Troop(1, NESeq(Vector(getQuickSoul(3), getQuickSoul(4), getQuickSoul(5))), Backpack.empty, userOne, move)
    val troopTwo = Troop(2, NESeq(toughSoul +: Vector.empty), Backpack.empty, userTwo, MovementPlan.idle(pos2))
    val start = Military(Vector(troopOne, troopTwo), Vector.empty, System.currentTimeMillis())

    val battleStart = System.currentTimeMillis() + Distance.tileToDistance(2) / quickSoulSample.speed
    val (firstRound, _) = start.tick(battleStart)
    val firstRoundEnd = battleStart + firstRound.events.head.asInstanceOf[Battle].round.duration
    val (secondRound, _) = firstRound.tick(firstRoundEnd)
    val secondRoundEnd = firstRoundEnd + secondRound.events.head.asInstanceOf[Battle].round.duration
    val (result, _) = secondRound.tick(secondRoundEnd + Distance.tileToDistance(1) / quickSoulSample.speed)

    result.objects.find(_.id == troopOne.id).get.pos(0) should be(pos3)
  }

  it should "not start battle for two cargos" in {
    val start = {
      val pos4 = Coordinate(3, 3)
      val moveOne = MovementPlan(Vector(Movement(pos4, pos1)), pos1)
      val moveTwo = MovementPlan(Vector(Movement(pos1, pos4)), pos4)
      val cargoOne = Cargo(1, userOne, NESeq(Vector(Guard.tiered(0)(userOne))), Vector(ItemStack(1, 2, defaultWeight)), moveOne)
      val cargoTwo = Cargo(2, userTwo, NESeq(Vector(Guard.tiered(0)(userTwo))), Vector(ItemStack(1, 2, defaultWeight)), moveTwo)
      Military(Vector(cargoOne, cargoTwo), Vector.empty, System.currentTimeMillis())
    }
    val samePositions = System.currentTimeMillis() + Distance.tileToDistance(2) / Guard.speed
    val meetingPos = Coordinate(2, 2)
    val (result, _) = start.tick(samePositions)
    val (c1 +: c2 +: _) = result.objects

    c1.pos(0) should be(meetingPos)
    c2.pos(0) should be(meetingPos)
    result.events should be(Vector.empty)
  }

  "Cargo" should "be generated by Outpost" in {
    val extractableYieldTime = 2000
    val start = {
      val user = User(userOne, "Vasya", City.empty(pos1.x, pos1.y))
      val extractable = Extractable(1, "Extr", 100, 1000, defaultWeight, Set.empty)
      val outpost = Outpost(pos2, user, "Test outpost", extractable, Vector.empty, extractableYieldTime)
      val (workingOutpost, _) = outpost.finish.run(getQuickSoul(1))
      Military(Vector(workingOutpost), Vector.empty, System.currentTimeMillis())
    }
    val firstYield = System.currentTimeMillis() + extractableYieldTime
    val secondYield = firstYield + extractableYieldTime
    val (mfirst, _) = start.tick(firstYield)
    val (mSecond, _) = mfirst.tick(secondYield)

    mfirst.objects.collect { case c: Cargo => c }.size should be(1)
    mSecond.objects.collect { case c: Cargo => c }.size should be(2)
  }

  "Troop" should "take loot from cargo" in {
    val expectedResources = ItemStack(1, 2, defaultWeight)
    val start = {
      val pos4 = Coordinate(3, 3)
      val moveOne = MovementPlan(Vector(Movement(pos4, pos1)), pos1)
      val moveTwo = MovementPlan(Vector(Movement(pos1, pos4)), pos4)
      val troopOne = Troop(1, NESeq(Vector(getSoulWithStrongWeapon(1))), Backpack.empty, userOne, moveOne)
      val cargoTwo = Cargo(2, userTwo, NESeq(Vector(Guard.tiered(0)(userTwo))), Vector(expectedResources), moveTwo)
      Military(Vector(troopOne, cargoTwo), Vector.empty, System.currentTimeMillis())
    }
    val battleStart = System.currentTimeMillis() + Distance.tileToDistance(2) / Guard.speed
    val (firstRound, _) = start.tick(battleStart)
    val firstRoundEnd = battleStart + firstRound.events.head.asInstanceOf[Battle].round.duration
    val (result, out) = firstRound.tick(firstRoundEnd)

    out.size should be(1)
    result.objects.head.asInstanceOf[Troop].backpack.getItem(2).get should be(expectedResources)
  }

  def getQuickSoul(id: ObjectId) = {
    val quickStats = Stats(Vector(StatProperty(Agility, Level(10, 0))))
    Soul(id, SoulData(Level(1, 0), 1, quickStats, Vector.empty), Equipment.empty)
  }

  def getSoulWithStrongWeapon(id: ObjectId) = {
    Soul(id, SoulData(Level(1, 0), 1, Stats(Vector.empty), Vector.empty), Equipment.empty.add(StubStrongWeapon())._1)
  }

}
