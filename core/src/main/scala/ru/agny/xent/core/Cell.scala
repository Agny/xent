package ru.agny.xent.core

import UserType._
import ru.agny.xent.core.city.{Building, City, Outpost}
import ru.agny.xent.core.inventory.Extractable

trait Cell {
  val x, y: Int
}

sealed trait Container {
  val building: Option[Facility]
}

sealed trait ContainerCell extends Cell with Container

case class WorldCell(x: Int, y: Int, building: Option[Outpost] = None, resource: Option[Extractable] = None, city: Option[City] = None, owner: Option[UserId] = None) extends ContainerCell
case class LocalCell(x: Int, y: Int, building: Option[Building] = None) extends ContainerCell

object WorldCell {
  def apply(x: Int, y: Int, mbRes: Option[Extractable]): WorldCell = WorldCell(x, y, None, mbRes)
}