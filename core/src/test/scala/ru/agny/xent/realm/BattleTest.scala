package ru.agny.xent.realm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.{Args, Status}
import ru.agny.xent.{Player, PlayerId, PlayerService, TimeInterval}
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{Backpack, Storage}
import ru.agny.xent.realm.map.{AICity, Battle, City, Troops}
import Battle.State
import ru.agny.xent.unit.Soul
import ru.agny.xent.utils.{ItemIdGenerator, PlayerIdGenerator}
import ru.agny.xent.war.{Defence, Fatigue, Sides}
import Hexagon._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.realm.Realm.{StrongTickPeriod, Timer}
import ru.agny.xent.realm.ai.TechonologyTier

class BattleTest extends AnyFlatSpec {

  given PlayerService = Player.defaultPS

  val PlayerOne = PlayerIdGenerator.next
  val PlayerTwo = PlayerIdGenerator.next
  val PlayerThree = PlayerIdGenerator.next
  val PlayerFour = PlayerIdGenerator.next

  "Battle.build" should "create battle" in {
    val troops = Seq(getTroops(PlayerOne), getTroops(PlayerTwo))
    val State(xs, np) = Battle.build(troops, Seq.empty)

    val expectedSides = Sides(troops.groupBy(_.owner))
    val expected = Seq(Battle(xs.head.id, expectedSides, Progress.Start(), troops.head.pos))

    xs shouldBe expected
    np shouldBe empty
  }

  it should "create only one battle" in {
    val troops = Seq(
      getTroops(PlayerOne),
      getTroops(PlayerTwo),
      getTroops(PlayerThree),
      getTroops(PlayerFour))

    val State(xs, np) = Battle.build(troops, Seq.empty)

    val expectedSides = Sides(troops.groupBy(_.owner))
    val expected = Seq(Battle(xs.head.id, expectedSides, Progress.Start(), troops.head.pos))

    xs shouldBe expected
    np shouldBe empty
  }

  it should "create two battles" in {
    given PlayerService = new PlayerService {
      override def isHostile(a: PlayerId, b: PlayerId) = {
        (a, b) match {
          case (PlayerOne, PlayerThree | PlayerFour) => false
          case (PlayerTwo, PlayerThree | PlayerFour) => false
          case (PlayerThree, PlayerOne | PlayerTwo) => false
          case (PlayerFour, PlayerOne | PlayerTwo) => false
          case _ => true
        }
      }
    }
    val troops = Seq(
      getTroops(PlayerOne),
      getTroops(PlayerTwo),
      getTroops(PlayerThree),
      getTroops(PlayerFour))
    val State(xs, np) = Battle.build(troops, Seq.empty)

    val expected = Seq(
      Battle(ItemIdGenerator.next, Sides(troops.take(2).groupBy(_.owner)), Progress.Start(), troops.head.pos),
      Battle(ItemIdGenerator.next, Sides(troops.drop(2).groupBy(_.owner)), Progress.Start(), troops.head.pos)
    )

    xs.map(_.sides) should contain theSameElementsAs expected.map(_.sides)
    np shouldBe empty
  }

  it should "join troops to battle" in {
    val troops = Seq(getTroops(PlayerOne), getTroops(PlayerTwo))
    val otherTroops = getTroops(PlayerThree)
    val sides = Sides(troops.groupBy(_.owner))
    val old = Battle(ItemIdGenerator.next, sides, Progress.Start(), troops.head.pos)
    val State(xs, np) = Battle.build(Seq(old, otherTroops), Seq.empty)

    val expected = Battle(
      ItemIdGenerator.next,
      Sides((otherTroops +: troops).groupBy(_.owner)),
      Progress.Start(),
      troops.head.pos
    )

    xs.size shouldBe 1
    xs.head.sides shouldBe expected.sides
    np shouldBe empty
  }

  it should "not create battle" in {
    val troops = Seq(getTroops(PlayerOne), getTroops(PlayerOne))
    val State(xs, np) = Battle.build(troops, Seq.empty)

    val expectedSides = Sides(troops.groupBy(_.owner))
    val expected = troops

    xs shouldBe empty
    np shouldBe expected
  }

  "Battle.tick" should "update progress and free troops" in {
    val troops = Seq(getTroops(PlayerOne), getTroops(PlayerTwo))
    val sides = Sides(troops.groupBy(_.owner))
    val battle = Battle(ItemIdGenerator.next, sides, Progress.Start(), troops.head.pos)
    val time = TimeInterval.BaseRound

    val expectedP = Progress.Start().copy(value = time - Progress.DefaultCap, cap = 0)
    val expected = Battle(battle.id, sides, expectedP, troops.head.pos)

    battle.tick(time)
    battle shouldBe expected
    battle.isFinished() shouldBe true
    battle.complete() should contain theSameElementsAs troops
  }

  private def getTroops(player: PlayerId): Troops = {
    Troops(ItemIdGenerator.next, player, Backpack.Empty, Seq(Soul.Empty), 2 ~ 2 ~> (1 ~ 1), Fatigue.Empty)
  }

}
