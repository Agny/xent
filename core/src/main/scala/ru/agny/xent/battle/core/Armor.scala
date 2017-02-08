package ru.agny.xent.battle.core

trait Armor extends Defensive {
  val name:String
  val attrs:Seq[Attribute]
}
