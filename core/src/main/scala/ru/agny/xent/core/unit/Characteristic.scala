package ru.agny.xent.core.unit

trait Characteristic {
  val value:Int
}

trait Primary extends Characteristic
trait Secondary extends Characteristic

case class Agility(value:Int) extends Primary
case class Strength(value:Int) extends Primary
case class Intelligence(value:Int) extends Primary
case class PresencePower(value:Int) extends Primary
case class CritRate(value: Int) extends Secondary
case class CritPower(value: Int) extends Secondary
case class Initiative(value: Int) extends Secondary
