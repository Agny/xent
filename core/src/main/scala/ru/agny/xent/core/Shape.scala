package ru.agny.xent.core

trait Shape {
  val core: Coordinate
  val parts: Vector[Coordinate]

  def form(around: Coordinate): ResultShape
}

case class ResultShape(core: Coordinate, private val parts: Vector[Coordinate]) {
  val cells = core +: parts

  def isIntersected(by: ResultShape): Boolean = cells.exists(by.cells.contains(_))
}

case class FourShape(core: Coordinate) extends Shape {
  val parts = Vector(
    Coordinate(0 + core.x, 1 + core.y),
    Coordinate(1 + core.x, 1 + core.y),
    Coordinate(1 + core.x, 0 + core.y)
  )

  override def form(around: Coordinate): ResultShape = {
    val s = FourShape(around)
    ResultShape(s.core, s.parts)
  }
}
