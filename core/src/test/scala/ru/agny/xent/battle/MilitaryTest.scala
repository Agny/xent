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

  "Military tick" should "create battles if needed" in {
    val start = {
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
      val quickStats = Stats(Vector(StatProperty(Agility, Level(10, 0))))
      val quickSoul = Soul(3, SoulData(Level(1, 0), Spirit(1, 0, 1), quickStats, Vector.empty), Equipment.empty)
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
