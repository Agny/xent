package ru.agny.xent.core

import ru.agny.xent.core.utils.ErrorCode

case class LifePower(points: Int, capacity: Int) {
  def spend(cost: Int): Either[ErrorCode.Value, LifePower] =
    if (cost > points) Left(ErrorCode.NOT_ENOUGH_LIFEPOWER)
    else Right(LifePower(points - cost, capacity))

  def regain(power: Int, growth: Int): LifePower = {
    val newCapacity = capacity + growth
    val updatedPoints = points + power
    val newPoints = if (updatedPoints > newCapacity) newCapacity else updatedPoints
    LifePower(newPoints, newCapacity)
  }
}

object LifePower {
  val default = LifePower(100, 100)
}
