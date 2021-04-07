package ru.agny.xent.realm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._
import org.scalatest.{Args, Status}
import ru.agny.xent.PlayerId
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

class ProgressTest extends AnyFlatSpec {

  "Progress" should "indicate overflow" in {
    val p = Progress(0, 10)
    val negative = p.fill(9)
    val positive1 = p.fill(1)
    val positive2 = p.fill(10)

    negative shouldBe false
    positive1 shouldBe true
    positive2 shouldBe true
  }

  it should "react to changed capacity" in {
    val p = Progress(0, 10)
    val negative1 = p.fill(9)
    p.updateCap(20)
    val negative2 = p.fill(1)
    val positive1 = p.fill(10)
    p.updateCap(5)
    val negative3 = p.fill(1)
    val positive2 = p.fill(4)

    negative1 shouldBe false
    negative2 shouldBe false
    negative3 shouldBe false
    positive1 shouldBe true
    positive2 shouldBe true
  }

}
