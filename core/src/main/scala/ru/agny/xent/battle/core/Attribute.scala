package ru.agny.xent.battle.core

sealed trait Attribute {
  val value: Int
}

trait Kinetic extends Attribute
trait Magic extends Attribute

trait Summon extends Magic {
  val power: Int
}
//trait Unique extends Attribute

trait Offensive
trait Defensive
