package ru.agny.xent.battle.core

case class Property(attr: Attribute, value: Int, mode: Mode) {
  val name: String = attr.getClass.getSimpleName

  def amplify(byPoints: Int): Property = Property(attr, value + byPoints, mode)
}

sealed trait Mode
case object Offensive extends Mode
case object Defensive extends Mode
