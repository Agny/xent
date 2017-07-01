package ru.agny.xent

case class LifePower(points: Int, capacity: Int) {
  def spend(cost: Int): Either[Response, LifePower] =
    if (cost < points) Left(Response("Not enough life power"))
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
