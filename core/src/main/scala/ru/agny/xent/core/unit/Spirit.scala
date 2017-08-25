package ru.agny.xent.core.unit

case class Spirit(points: Int, regen: Int, capacity: Int) {
  def change(x: Int) = Spirit(points + x, regen, capacity)

  def toLifePower: Int = capacity
}

case class SpiritBase(regen: Int, capacity: Int)