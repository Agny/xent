package ru.agny.xent.core

import ru.agny.xent.City
import ru.agny.xent.UserType._

sealed trait Cell {
  val x, y: Int
  val building: Option[Facility]

  override def toString: String = s"[$x,$y]"
}

case class WorldCell(x: Int, y: Int, building: Option[Outpost] = None, resource: Option[Extractable] = None, city: Option[City] = None, owner: Option[UserId] = None) extends Cell
case class LocalCell(x: Int, y: Int, building: Option[Building] = None) extends Cell

object WorldCell {
  def apply(x: Int, y: Int, mbRes: Option[Extractable]): WorldCell = WorldCell(x, y, None, mbRes)
}