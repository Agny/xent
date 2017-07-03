package ru.agny.xent.core.unit

sealed trait Characteristic extends Levelable {
  val name: String = getClass.getSimpleName
  val level: Level
  val weightModifier: Int

  def toLifePower: Int = {
    val expBonus = level.exp * weightModifier / level.capacity
    scaledWeight + expBonus
  }

  private def scaledWeight = {
    val step = 10
    val scale = level.value / step
    val tiersWeight = (1 + scale) * (scale * step) * weightModifier / 2
    val levelsWeight = (1 + scale) * (level.value - (scale * step)) * weightModifier
    tiersWeight + levelsWeight
  }
}

sealed trait Primary extends Characteristic {
  //TODO game balancing
  val weightModifier = 10
}
sealed trait Secondary extends Characteristic {
  //TODO game balancing
  val weightModifier = 2
}

case class Agility(level: Level) extends Primary
case class Strength(level: Level) extends Primary
case class Intelligence(level: Level) extends Primary
case class PresencePower(level: Level) extends Primary
case class CritRate(level: Level) extends Secondary
case class CritPower(level: Level) extends Secondary
case class Initiative(level: Level) extends Secondary
