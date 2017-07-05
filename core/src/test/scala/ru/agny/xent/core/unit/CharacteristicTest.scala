package ru.agny.xent.core.unit

import org.scalatest.{FlatSpec, Matchers}
import ru.agny.xent.core.unit.characteristic.{CritPower, Agility}
import ru.agny.xent.core.unit.equip.StatProperty

class CharacteristicTest extends FlatSpec with Matchers {

  "Any characteristic" should "convert itself to life power" in {
    val primary = StatProperty(Agility, Level(2, 52))
    val secondary = StatProperty(CritPower, Level(36, 641))

    primary.toLifePower should be(24)
    secondary.toLifePower should be(169)
  }

}
