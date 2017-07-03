package ru.agny.xent.core.unit

import org.scalatest.{FlatSpec, Matchers}

class CharacteristicTest extends FlatSpec with Matchers {

  "Any characteristic" should "convert itself to life power" in {
    val primary = Agility(Level(2, 52))
    val secondary = CritPower(Level(36, 641))

    primary.toLifePower should be(24)
    secondary.toLifePower should be(169)
  }

}
