package ru.agny.xent.core.unit

import ru.agny.xent.core.LifePowered

case class Spirit(points: Int, base: SpiritBase) extends LifePowered {
  def change(x: Int) = points + x

  def toLifePower: Int = base.capacity
}