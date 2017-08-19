package ru.agny.xent.battle

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.TestHelper
import ru.agny.xent.battle.unit.{Backpack, Troop}
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.unit.characteristic.PresencePower
import ru.agny.xent.core.unit.equip.{StatProperty, Equipment}
import ru.agny.xent.core.unit._
import ru.agny.xent.core.utils.NESeq

class BattleTest extends FlatSpec with Matchers with EitherValues {

  import TestHelper._

  val pos = MovementPlan.idle(Coordinate(1, 1))

  val userOne = 1
  val userTwo = 2
  val soulOne = defaultSoul(1)

  "Battle" should "add troops to the queue" in {
    val (start, queue) = {
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulOne +: Vector.empty), Backpack.empty, userTwo, pos)
      val troopThree = Troop(3, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopQueue = Troop(4, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troops = Vector(troopOne, troopTwo, troopThree)
      (Battle(pos.home, NESeq(troops)), Vector(troopQueue))
    }
    val updated = start.addTroops(queue)
    updated.troops should contain allElementsOf (start.troops ++ queue)
  }

  "Battle tick" should "return current instance if the round time still remains" in {
    val start = {
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulOne +: Vector.empty), Backpack.empty, userTwo, pos)
      val troopThree = Troop(3, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troops = Vector(troopOne, troopTwo, troopThree)
      Battle(pos.home, NESeq(troops))
    }
    val (mbBattle, out) = start.tick()
    mbBattle.get should be theSameInstanceAs start
    out should be(Vector.empty)
  }

  it should "end battle in one round" in {
    val start = {
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(defaultSoul(2) +: Vector.empty), Backpack.empty, userTwo, pos)
      val troops = Vector(troopOne, troopTwo)
      Battle(pos.home, NESeq(troops))
    }
    val r = start.round
    val (result, out) = start.tick(System.currentTimeMillis() + r.duration + 10)
    out.count(x => x.isActive) should be(1)
    result should be(None)
  }

  it should "end battle in two or three rounds" in {
    val start = {
      val toughSoul = Soul(2, SoulData(Level(1, 1), Spirit(10, 1, 10), Stats(Vector(StatProperty(PresencePower, Level(20, 0)))), Vector.empty), Equipment.empty)
      val soulThree = Soul(3, SoulData(Level(1, 1), Spirit(3, 1, 3), Stats(Vector(StatProperty(PresencePower, Level(5, 0)))), Vector.empty), Equipment.empty)
      val soulFour = Soul(4, SoulData(Level(1, 1), Spirit(1, 1, 1), Stats(Vector(StatProperty(PresencePower, Level(5, 0)))), Vector.empty), Equipment.empty)
      val userThree = 3
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(toughSoul +: Vector.empty), Backpack.empty, userTwo, pos)
      val troopThree = Troop(3, NESeq(soulThree +: Vector.empty), Backpack.empty, userThree, pos)
      val troopFour = Troop(4, NESeq(soulFour +: Vector.empty), Backpack.empty, userThree, pos)
      val troops = Vector(troopOne, troopTwo, troopThree, troopFour)
      Battle(pos.home, NESeq(troops))
    }

    val (second, outFirst) = start.tick(System.currentTimeMillis() + start.round.duration)
    val (third, outSecond) = second.get.tick(System.currentTimeMillis() + second.get.round.story + second.get.round.duration)
    if (outFirst.size == 1 && outSecond.size == 1) {
      val (last, outThird) = third.get.tick(System.currentTimeMillis() + third.get.round.story + third.get.round.duration)
      (outFirst ++ outSecond ++ outThird).size should be(4)
      last should be(None)
    } else {
      (outFirst ++ outSecond).size should be(4)
      third should be(None)
    }
  }

}
