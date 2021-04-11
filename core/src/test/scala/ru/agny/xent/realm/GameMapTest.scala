package ru.agny.xent.realm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.{Args, Status}
import ru.agny.xent.{Player, PlayerId, PlayerService}
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{Backpack, Storage}
import ru.agny.xent.realm.map.{AICity, Battle, City, Troops}
import ru.agny.xent.unit.Soul
import ru.agny.xent.utils.{ItemIdGenerator, PlayerIdGenerator}
import ru.agny.xent.war.{Defence, Fatigue, Sides}
import Hexagon._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.realm.Realm.{StrongTickPeriod, Timer}
import ru.agny.xent.realm.ai.TechonologyTier

class GameMapTest extends AnyFlatSpec {
  val maxX = 10
  val maxY = 10
  val playerOne = PlayerIdGenerator.next
  val playerTwo = PlayerIdGenerator.next
  val timer = Timer(System.currentTimeMillis() - StrongTickPeriod * 1000)

  given PlayerService = Player.defaultPS

  "GameMap" should "init state by input parameters" in {
    val freightMovement = 0 ~ 0 ~> (2 ~ 2)
    val roamingMovement = 5 ~ 5 ~> (2 ~ 2)
    val freight = getTroops(playerOne, freightMovement)
    val roaming = getTroops(playerTwo, roamingMovement)
    val troops = Seq(freight, roaming)

    val city = City(ItemIdGenerator.next, playerOne, Buildings.Default, Defence.Empty, 0 ~ 0)
    val aICity = AICity(
      ItemIdGenerator.next,
      Defence.Empty,
      Storage.Empty,
      7 ~ 0)
    val places = Seq(city, aICity)

    val gm = GameMap(maxX, maxY, places, troops)

    gm.getState() should contain theSameElementsAs places
    gm.getTroops() should contain theSameElementsAs troops
  }

  it should "remove destructed" in {
    val city = City(ItemIdGenerator.next, playerOne, Buildings.Default, Defence.Empty, 0 ~ 0)
    val aICity = AICity(ItemIdGenerator.next, Defence.Empty, Storage.Empty, 7 ~ 0)
    val places = Seq(city, aICity)

    val gm = GameMap(maxX, maxY, places, Seq.empty)
    gm.tick(timer)

    gm.getState() shouldBe Seq(city)
  }

  it should "return troops upon battle completion" in {
    val timer = Timer(System.currentTimeMillis() - Progress.DefaultCap * 1000)

    val battleTroops = Seq(
      getTroops(playerOne, 2 ~ 2 ~> (1 ~ 1)),
      getTroops(playerTwo, 2 ~ 1 ~> (1 ~ 1))
    )
    val roaming = getTroops(playerTwo, 0 ~ 0 ~> (4 ~ 4))
    val sides = battleTroops.groupMap(_.owner)(x => x)

    val b = Battle(ItemIdGenerator.next, Sides(sides), Progress.Start(), 0 ~ 0)
    val military = Seq(b, roaming)

    val gm = GameMap(maxX, maxY, Seq.empty, military)
    gm.tick(timer)

    val expectedTemporal = roaming +: battleTroops

    gm.getState() shouldBe Seq.empty
    gm.getTroops() should contain theSameElementsAs expectedTemporal
  }

  private def getTroops(player: PlayerId, movement: Movement): Troops = {
    Troops(ItemIdGenerator.next, player, Backpack.Empty, Seq(Soul.Empty), movement, Fatigue.Empty)
  }

}
