package ru.agny.xent.battle

import org.scalatest.{EitherValues, Matchers, FlatSpec}
import ru.agny.xent.TestHelper
import ru.agny.xent.battle.unit.{Backpack, Troop}
import ru.agny.xent.core.Coordinate
import ru.agny.xent.core.utils.NESeq

class RoundTest extends FlatSpec with Matchers with EitherValues {

  import TestHelper._

  val pos = MovementPlan.idle(Coordinate(1, 1))
  val userOne = 1
  val userTwo = 2
  val soulOne = defaultSoul(1)
  val soulTwo = defaultSoul(2)

  "Round" should "throw exception if there are no units" in {
    assertThrows[UnsupportedOperationException](Round(1, NESeq(Vector.empty), 0))
  }

  it should "have zero duration if all troops belongs to the same user" in {
    val start = {
      val troopOne = Troop(1, NESeq(soulOne, Vector.empty), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulTwo, Vector.empty), Backpack.empty, userOne, pos)
      Round(1, NESeq(Vector(troopOne, troopTwo)))
    }

    start.duration should be(0)
  }

  it should "calculate duration based on weights of users armies" in {
    val (start, relation) = {
      val userThree = 3
      val soulThree = defaultSoul(3)
      val soulFour = defaultSoul(4)
      val troopOne = Troop(1, NESeq(Vector(soulOne, soulTwo)), Backpack.empty, userOne, pos)
      val troopTwo = Troop(2, NESeq(soulThree, Vector.empty), Backpack.empty, userTwo, pos)
      val troopThree = Troop(3, NESeq(soulFour, Vector.empty), Backpack.empty, userThree, pos)
      (Round(1, NESeq(Vector(troopOne, troopTwo, troopThree))), troopTwo.weight.toDouble / troopOne.weight)
    }

    start.duration should be(relation * Round.timeLimitMax)
  }

}
