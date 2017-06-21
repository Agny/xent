package ru.agny.xent.core

sealed trait Shape {
  val name: String
  def form(around: Coordinate): ResultShape
}

case class ResultShape(core: Coordinate, private val parts: Vector[Coordinate]) {
  val cells = core +: parts

  def isIntersected(by: ResultShape): Boolean = cells.exists(by.cells.contains(_))
}

object Shape {
  case object FourShape extends Shape {
    override val name = "SquareFour"

    override def form(around: Coordinate): ResultShape = {
      val core = around
      val parts = Vector(
        Coordinate(0 + core.x, 1 + core.y),
        Coordinate(1 + core.x, 1 + core.y),
        Coordinate(1 + core.x, 0 + core.y)
      )
      ResultShape(core, parts)
    }
  }

  val values = Map(FourShape.name -> FourShape)
}
