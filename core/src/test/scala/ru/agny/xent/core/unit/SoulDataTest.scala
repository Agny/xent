package ru.agny.xent.core.unit

import org.scalatest.{Matchers, FlatSpec}
import ru.agny.xent.core.unit.characteristic._
import ru.agny.xent.core.unit.equip.attributes.Blunt
import ru.agny.xent.core.unit.equip.{Offensive, AttrProperty, Equipment, StatProperty}

class SoulDataTest extends FlatSpec with Matchers {

  val level = Level(25, 0)
  val stats = Stats(Vector(
    StatProperty(Strength, level),
    StatProperty(PresencePower, level),
    StatProperty(Agility, level),
    StatProperty(Intelligence, level),
    StatProperty(Initiative, level)
  ))
  val soulData = SoulData(Level(1, 1), 10, stats, Vector.empty)
  val eq = Equipment.empty

  "Stats" should "calculate armor" in {
    soulData.armor(eq) should be(9)
  }

  it should "calculate spirit" in {
    val expected = Spirit(10, SpiritBase(3, 50))
    soulData.spirit(eq) should be(expected)
  }

  it should "calculate speed" in {
    soulData.speed(eq) should be(Speed.default + 12)
  }

  it should "calculate endurance" in {
    soulData.endurance(eq) should be(Endurance.default + 5)
  }

  it should "calculate initiative" in {
    soulData.initiative(eq) should be(25)
  }


  it should "calculate attack modifiers" in {
    val (_, rates) = soulData.attackModifiers(eq).head
    val (attr, modifiers) = rates.unzip
    attr should contain(AttrProperty(Blunt, 1, Offensive))
    all(modifiers) should (be >= 25 and be <= 45) //check damage calculation for default weapon Blunt
  }

  it should "calculate spirit damage" in {
    val expected1 = Spirit(5, SpiritBase(3, 50))
    val expected2 = Spirit(2, SpiritBase(3, 50))
    val updated = soulData.receiveDamage(5)(eq)
    val next = updated.receiveDamage(3)(eq)
    updated.spirit(eq) should be(expected1)
    next.spirit(eq) should be(expected2)
  }

  it should "gain exp by damage received" in {
    val expected1 = Level(1, 6)
    val expected2 = Level(1, 11)
    val updated = soulData.receiveDamage(5)(eq)
    val overkill = updated.receiveDamage(15)(eq)
    updated.level should be(expected1)
    overkill.level should be(expected2)
  }

}
