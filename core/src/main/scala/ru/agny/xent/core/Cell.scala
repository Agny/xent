package ru.agny.xent.core

import ru.agny.xent.City
import ru.agny.xent.UserType._

sealed trait Cell {
  val x, y: Int
}

sealed trait Container {
  val building: Option[Facility]
}

sealed trait ContainerCell extends Cell with Container

case class WorldCell(x: Int, y: Int, building: Option[Outpost] = None, resource: Option[Extractable] = None, city: Option[City] = None, owner: Option[UserId] = None) extends ContainerCell
case class LocalCell(x: Int, y: Int, building: Option[Building] = None) extends ContainerCell
case class Coordinate(x: Int, y: Int) extends Cell

object WorldCell {
  def apply(x: Int, y: Int, mbRes: Option[Extractable]): WorldCell = WorldCell(x, y, None, mbRes)
}