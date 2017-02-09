package ru.agny.xent.battle.core

sealed trait Attribute {
  //TODO name?
  val name: String = getClass.getSimpleName
  val value: Int
}

trait Kinetic extends Attribute
trait Magic extends Attribute

trait Summon extends Magic {
  val power: Int
}
//trait Unique extends Attribute

trait Equippable {
  val name: String
  val attrs: Seq[Attribute]
}
trait Offensive extends Equippable
trait Defensive extends Equippable
