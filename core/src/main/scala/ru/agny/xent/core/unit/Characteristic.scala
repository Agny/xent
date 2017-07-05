package ru.agny.xent.core.unit

sealed trait Characteristic {
  val name: String = getClass.getSimpleName
  val weightModifier: Int
}

sealed trait Primary extends Characteristic {
  //TODO game balancing
  val weightModifier = 10
}
sealed trait Secondary extends Characteristic {
  //TODO game balancing
  val weightModifier = 2
}

package object characteristic {
  case object Agility extends Primary
  case object Strength extends Primary
  case object Intelligence extends Primary
  case object PresencePower extends Primary
  case object CritRate extends Secondary
  case object CritPower extends Secondary
  case object Initiative extends Secondary
}