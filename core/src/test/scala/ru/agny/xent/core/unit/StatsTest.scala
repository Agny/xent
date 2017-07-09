package ru.agny.xent.core.unit

import org.scalatest.{Matchers, FlatSpec}
import ru.agny.xent.core.unit.characteristic._
import ru.agny.xent.core.unit.equip.attributes.Blunt
import ru.agny.xent.core.unit.equip.{Offensive, AttrProperty, Equipment, StatProperty}

class StatsTest extends FlatSpec with Matchers {

  val level = Level(25, 0)
  val stats = Stats(Vector(
    StatProperty(Strength, level),
    StatProperty(PresencePower, level),
    StatProperty(Agility, level),
    StatProperty(Intelligence, level),
    StatProperty(Initiative, level)
  ))
  val eq = Equipment.empty

  "Stats" should "calculate armor" in {
    stats.effectiveArmor(eq) should be(9)
  }

  it should "calculate spirit" in {
    val baseSpirit = Spirit(10, 1, 10)
    val expected = Spirit(10, 4, 60)
    stats.effectiveSpirit(eq, baseSpirit) should be(expected)
  }

  it should "calculate speed" in {
    stats.effectiveSpeed(eq) should be(Speed.default + 12)
  }

  it should "calculate endurance" in {
    stats.effectiveEndurance(eq) should be(Endurance.default + 5)
  }

  it should "calculate initiative" in {
    stats.effectiveInitiative(eq) should be(25)
  }


  it should "calculate attack modifiers" in {
    val (_, rates) = stats.attackModifiers(eq).head
    val (attr, modifiers) = rates.unzip
    attr should contain(AttrProperty(Blunt, 1, Offensive))
    all(modifiers) should (be >= 25 and be <= 45) //check damage calculation for default weapon Blunt
  }

}
