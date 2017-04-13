package ru.agny.xent.core

trait Shape {
  val core: LocalCell
  val parts: Vector[Cell]

  def form(around: LocalCell): Shape
}

case class FourShape(core: LocalCell) extends Shape {
  val parts = Vector(
    Coordinate(0 + core.x, 1 + core.y),
    Coordinate(1 + core.x, 1 + core.y),
    Coordinate(1 + core.x, 0 + core.y)
  )

  override def form(around: LocalCell): Shape = FourShape(around)
}
