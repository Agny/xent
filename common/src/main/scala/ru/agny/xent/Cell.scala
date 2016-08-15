package ru.agny.xent

trait Cell {
  val x, y: Int

  override def toString: String = s"[$x,$y]"
}

case class BasicCell(x: Int, y: Int) extends Cell
