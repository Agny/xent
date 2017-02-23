package ru.agny.xent.battle.unit

trait Characteristic {
  val value:Int
}

case class Agility(value:Int) extends Characteristic
case class Strength(value:Int) extends Characteristic
case class Intelligence(value:Int) extends Characteristic
case class PresencePower(value:Int) extends Characteristic
