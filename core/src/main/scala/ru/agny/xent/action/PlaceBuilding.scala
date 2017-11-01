package ru.agny.xent.action

import ru.agny.xent.core.city.Building
import ru.agny.xent.core.utils.{BuildingTemplate, ErrorCode}
import ru.agny.xent.core.{Coordinate, Layer, User}
import ru.agny.xent.messages.{ReactiveLog, ResponseOk}

case class PlaceBuilding(facility: String, layer: Layer, cell: Coordinate, src: ReactiveLog) extends UserAction {
  override def run(user: User) = {
    val bt = layer.facilities.collectFirst { case bt: BuildingTemplate if bt.name == facility => bt }
    bt match {
      case Some(x) =>
        val b = Building(cell, x.name, x.producibles, x.buildTime)
        user.build(b, x.cost) match {
          case Left(v) => src.failed(v); user
          case Right(v) => src.respond(ResponseOk); v
        }
      case None => src.failed(ErrorCode.BUILDING_SCHEMA_NOT_EXIST); user
    }
  }
}
