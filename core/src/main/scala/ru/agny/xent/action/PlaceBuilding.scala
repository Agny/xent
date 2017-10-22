package ru.agny.xent.action

import ru.agny.xent.core.city.Building
import ru.agny.xent.core.utils.BuildingTemplate
import ru.agny.xent.core.{Coordinate, Layer, User}
import ru.agny.xent.messages.PlainResponse

case class PlaceBuilding(facility: String, layer: Layer, cell: Coordinate) extends UserAction {
  override def run(user: User) = {
    val bt = layer.facilities.collectFirst { case bt: BuildingTemplate if bt.name == facility => bt }
    bt.map(x => {
      val b = Building(cell, x.name, x.producibles, x.buildTime)
      user.build(b, x.cost)
    }) getOrElse Left(PlainResponse(s"Unable to build $facility"))
  }
}
