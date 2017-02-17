package ru.agny.xent.battle.core

case class Property(attr: Attribute, value: Int, mode: Mode) {
  val name: String = attr.getClass.getSimpleName

  def amplify(byPoints: Int): Property = Property(attr, value + byPoints, mode)
}

trait Equippable {
  val name: String
  val attrs: Seq[Property]
}

sealed trait Mode
case object Offensive extends Mode
case object Defensive extends Mode
