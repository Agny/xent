package ru.agny.xent

import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core.utils.BuildingTemplate
import ru.agny.xent.core._

trait UserAction extends Action {
  type T = User

  override def run(user: T): Either[Response, T]
}

object DoNothing extends UserAction {
  override def run(user: User): Either[Response, User] = Right(user)
}

case class PlaceBuilding(facility: String, layer: Layer, cell: Coordinate) extends UserAction {
  override def run(user: User): Either[Response, User] = {
    val bt = layer.facilities.collectFirst { case bt: BuildingTemplate if bt.name == facility => bt }
    bt.map(x => {
      val b = Building(x.name, x.producibles, x.buildTime)
      user.build(LocalCell(cell.x, cell.y, Some(b)), x.cost)
    }) getOrElse Left(Response(s"Unable to build $facility"))
  }
}

case class AddProduction(facility: ItemId, res: ItemStack) extends UserAction {
  override def run(user: User): Either[Response, User] = user.addProduction(facility, res)
}
