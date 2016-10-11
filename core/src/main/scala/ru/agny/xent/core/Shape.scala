package ru.agny.xent.core

trait Shape {
  val core: LocalCell
  val parts: Seq[LocalCell]

  def form(around: LocalCell): Shape
}

case class FourShape(core: LocalCell) extends Shape {
  val parts = Seq(
    LocalCell(0 + core.x, 1 + core.y),
    LocalCell(1 + core.x, 1 + core.y),
    LocalCell(1 + core.x, 0 + core.y)
  )

  override def form(around: LocalCell): Shape = FourShape(around)
}
