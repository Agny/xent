package ru.agny.xent.battle.core

trait Weapon extends Offensive {
  val name:String
  val attrs:Seq[Attribute]
}

