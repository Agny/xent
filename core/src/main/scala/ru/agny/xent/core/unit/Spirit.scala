package ru.agny.xent.core.unit

case class Spirit(points: Int, base: SpiritBase) {
  def change(x: Int) = points + x

  def toLifePower: Int = base.capacity
}

case class SpiritBase(regen: Int, capacity: Int)