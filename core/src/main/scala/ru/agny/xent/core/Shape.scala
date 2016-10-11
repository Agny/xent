package ru.agny.xent.core

trait Shape {
  val core: LocalCell
  val parts: Seq[LocalCell]

  def building: Option[Building]
}

case class BuildingShape(b: Building, core: LocalCell, parts: Seq[LocalCell]) extends Shape {
  override def building: Option[Building] = Some(b)
}

case class SimpleShape(core: LocalCell, parts: Seq[LocalCell]) extends Shape {
  override def building: Option[Building] = None
}
