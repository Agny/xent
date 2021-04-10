package ru.agny.xent.realm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.{Args, Status}
import ru.agny.xent.{Player, PlayerId, PlayerService, TimeInterval}
import ru.agny.xent.city.Buildings
import ru.agny.xent.item.{Backpack, Equipment, Storage}
import ru.agny.xent.realm.map.{AICity, Battle, City, Troops}
import Battle.State
import ru.agny.xent.unit.{Level, Soul, SoulData, Spirit}
import ru.agny.xent.utils.{ItemIdGenerator, PlayerIdGenerator}
import ru.agny.xent.war.{Defence, Fatigue, Sides}
import Hexagon._
import ru.agny.xent.Player.AIEnemy
import ru.agny.xent.realm.Realm.{StrongTickPeriod, Timer}
import ru.agny.xent.realm.ai.TechonologyTier

class SidesTest extends AnyFlatSpec {

  given PlayerService = Player.defaultPS

  val PlayerOne = PlayerIdGenerator.next
  val PlayerTwo = PlayerIdGenerator.next

  "Sides.getRoundLength" should "return zero" in {
    val troops = Seq(getEmptyTroops(PlayerOne), getEmptyTroops(PlayerTwo)).groupMap(_.owner)(x => x)
    val s = Sides(troops)
    s.getRoundLength() shouldBe TimeInterval.Zero
  }

  it should "return minimal round length" in {
    val troops = Seq(getTroops(PlayerOne), getPowerfulTroops(PlayerTwo)).groupMap(_.owner)(x => x)
    val s = Sides(troops)
    s.getRoundLength() shouldBe TimeInterval.BaseRound
  }

  it should "return max round length" in {
    val troops = Seq(getPowerfulTroops(PlayerOne), getPowerfulTroops(PlayerTwo)).groupMap(_.owner)(x => x)
    val s = Sides(troops)
    s.getRoundLength() shouldBe TimeInterval.BaseRound * 2
  }

  "Sides.round" should "initiate attack sequence" in {
    val t1 = getPowerfulTroops(PlayerOne)
    val t2 = getPowerfulTroops(PlayerTwo)
    val troops = Seq(t1, t2).groupMap(_.owner)(x => x)
    val s = Sides(troops)
    s.round()

    t1.units.head.spirit() shouldBe (PSpirit - Soul.DefaultDamage)
    t2.units.head.spirit() shouldBe (PSpirit - Soul.DefaultDamage)
  }

  private def getEmptyTroops(player: PlayerId): Troops = {
    Troops(ItemIdGenerator.next, player, Backpack.Empty, Seq.empty[Soul], 2 ~ 2 ~> (1 ~ 1), Fatigue.Empty)
  }

  private def getTroops(player: PlayerId): Troops = {
    val sd = SoulData(Level(10, 100), Spirit(1, 50, 150))
    Troops(
      ItemIdGenerator.next,
      player,
      Backpack.Empty,
      Seq(Soul(ItemIdGenerator.next, sd, Equipment.Empty)),
      2 ~ 2 ~> (1 ~ 1),
      Fatigue.Empty)
  }

  val PSpirit = 1000

  private def getPowerfulTroops(player: PlayerId): Troops = {
    val sd = SoulData(Level(10, 100), Spirit(PSpirit, 100, 150))
    Troops(
      ItemIdGenerator.next,
      player,
      Backpack.Empty,
      Seq(Soul(ItemIdGenerator.next, sd, Equipment.Empty)),
      2 ~ 2 ~> (1 ~ 1),
      Fatigue.Empty)
  }
}
