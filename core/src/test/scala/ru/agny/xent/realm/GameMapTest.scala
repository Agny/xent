package ru.agny.xent.realm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.{Args, Status}
import ru.agny.xent.PlayerId
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{Backpack, Storage}
import ru.agny.xent.realm.map.{AICity, City, Troops}
import ru.agny.xent.unit.Soul
import ru.agny.xent.utils.{ItemIdGenerator, PlayerIdGenerator}
import ru.agny.xent.war.{Defence, Fatigue}
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

  "GameMap" should "init state by input parameters" in {
    val freightMovement = 0 ~ 0 ~> (2 ~ 2)
    val roamingMovement = 5 ~ 5 ~> (2 ~ 2)
    val freight = getTroops(playerOne, freightMovement)
    val roaming = getTroops(playerTwo, roamingMovement)
    val troops = Seq(freight, roaming)

    val city = City(ItemIdGenerator.next, Some(playerOne), Buildings.Default, Defence.Empty, 0 ~ 0)
    val aICity = AICity(
      ItemIdGenerator.next,
      Some(AIEnemy.id),
      TechonologyTier.Default,
      Defence.Empty,
      Storage.Empty,
      7 ~ 0)
    val places = Seq(city, aICity)

    val gm = GameMap(maxX, maxY, places, troops)

    gm.getState() should contain theSameElementsAs places
    gm.getTroops() should contain theSameElementsAs troops
  }

  it should "remove destructed" in {
    val city = City(ItemIdGenerator.next, Some(playerOne), Buildings.Default, Defence.Empty, 0 ~ 0)
    val aICity = AICity(ItemIdGenerator.next, Some(AIEnemy.id), TechonologyTier.Default, Defence.Empty, Storage
      .Empty, 7 ~ 0)
    val places = Seq(city, aICity)

    val gm = GameMap(maxX, maxY, places, Seq.empty)
    gm.tick(timer)

    gm.getState() shouldBe Seq(city)
  }

  private def getTroops(player: PlayerId, movement: Movement): Troops = {
    Troops(ItemIdGenerator.next, Some(player), Backpack.Empty, Seq(Soul.Empty), movement, Fatigue.Empty)
  }

}
