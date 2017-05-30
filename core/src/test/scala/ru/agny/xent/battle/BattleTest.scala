package ru.agny.xent.battle

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.battle.core.LevelBar
import ru.agny.xent.battle.unit.{SpiritBar, Soul, Troop}
import ru.agny.xent.battle.unit.inventory.{Equipment, Backpack}
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.utils.NESeq

class BattleTest extends FlatSpec with Matchers with EitherValues {

  val pos = Coordinate(1, 1)
  val defaultOccupation = new Waiting(pos, System.currentTimeMillis())

  "Battle" should "add troops to the queue" in {
    val (start, queue) = {
      val userOne = 1
      val userTwo = 2
      val dummySoul = Soul(1, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(dummySoul +: Vector.empty), Backpack.empty, userTwo, pos)
      val troopThree = Troop(3, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, pos)
      val troopQueue = Troop(4, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, pos)
      val troops = Vector(troopOne, troopTwo, troopThree).map(_ -> defaultOccupation)
      (Battle(pos, NESeq(troops)), Vector(troopQueue -> defaultOccupation))
    }
    val updated = start.addTroops(queue)
    updated.troops should contain allElementsOf (start.troops ++ queue.unzip._1)
  }

  "Battle tick" should "return current instance if the round time still remains" in {
    val start = {
      val userOne = 1
      val userTwo = 2
      val dummySoul = Soul(1, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(dummySoul +: Vector.empty), Backpack.empty, userTwo, pos)
      val troopThree = Troop(3, NESeq(dummySoul +: Vector.empty), Backpack.empty, userOne, pos)
      val troops = Vector(troopOne, troopTwo, troopThree).map(_ -> defaultOccupation)
      Battle(pos, NESeq(troops))
    }
    val (mbBattle, out) = start.tick()
    mbBattle.get should be theSameInstanceAs start
    out should be(Vector.empty)
  }

  it should "end battle in one round" in {
    val start = {
      val userOne = 1
      val userTwo = 2
      val soulOne = Soul(1, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulTwo = Soul(2, LevelBar(1, 1, 1), SpiritBar(10, 1, 10), Equipment.empty, 10, Vector.empty)
      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulTwo +: Vector.empty), Backpack.empty, userTwo, pos)
      val troops = Vector(troopOne, troopTwo).map(_ -> defaultOccupation)
      Battle(pos, NESeq(troops))
    }
    val r = start.round
    val (result, out) = start.tick(System.currentTimeMillis() + r.duration + 10)
    out.count(x => x._1.isActive) should be(1)
    result should be(None)
  }

  it should "end battle in two or three rounds" in {
    val start = {
      val userOne = 1
      val userTwo = 2
      val userThree = 3
      val soulOne = Soul(1, LevelBar(1, 1, 1), SpiritBar(1, 1, 1), Equipment.empty, 10, Vector.empty)
      val soulTwo = Soul(2, LevelBar(1, 1, 1), SpiritBar(10, 1, 10), Equipment.empty, 10, Vector.empty)
      val soulThree = Soul(userThree, LevelBar(1, 1, 1), SpiritBar(1, 1, 10), Equipment.empty, 10, Vector.empty)
      val soulFour = Soul(userThree, LevelBar(1, 1, 1), SpiritBar(1, 1, 10), Equipment.empty, 10, Vector.empty)

      val troopOne = Troop(1, NESeq(soulOne +: Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulTwo +: Vector.empty), Backpack.empty, userTwo, pos)
      val troopThree = Troop(3, NESeq(soulThree +: Vector.empty), Backpack.empty, userThree, pos)
      val troopFour = Troop(4, NESeq(soulFour +: Vector.empty), Backpack.empty, userThree, pos)
      val troops = Vector(troopOne, troopTwo, troopThree, troopFour).map(_ -> defaultOccupation)
      Battle(pos, NESeq(troops))
    }

    val (second, outFirst) = start.tick(System.currentTimeMillis() + start.round.duration)
    val (third, outSecond) = second.get.tick(System.currentTimeMillis() + second.get.round.story + second.get.round.duration)
    if (outFirst.size == 1) {
      val (last, outThird) = third.get.tick(System.currentTimeMillis() + third.get.round.story + third.get.round.duration)
      (outFirst ++ outSecond ++ outThird).size should be(4)
      last should be(None)
    } else {
      (outFirst ++ outSecond).size should be(4)
      third should be(None)
    }
  }

}
