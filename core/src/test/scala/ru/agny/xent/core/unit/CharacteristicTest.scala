package ru.agny.xent.core.unit

import org.scalatest.{FlatSpec, Matchers}
import ru.agny.xent.core.unit.Characteristic.{CritPower, Agility}
import ru.agny.xent.core.unit.equip.StatProperty
import ru.agny.xent.core.unit.equip.Attribute.Piercing

class CharacteristicTest extends FlatSpec with Matchers {

  "Any characteristic" should "convert itself to life power" in {
    val primary = StatProperty(Agility, Level(2, 52))
    val secondary = StatProperty(CritPower, Level(36, 641))

    primary.toLifePower should be(24)
    secondary.toLifePower should be(169)
  }

  "StatProperty" should "calculate bonus damage" in {
    val agility = StatProperty(Agility, Level(20, 90))
    val times = 100
    for (_ <- 1 to times) {
      val bonus = agility.bonusDamage(Piercing)
      val (minValue, maxValue) = (6, 9) // (10d1 cast + 10d2 cast) * PiercingModifier(=0.3)
      bonus should (be >= minValue and be <= maxValue)
    }
  }

}
