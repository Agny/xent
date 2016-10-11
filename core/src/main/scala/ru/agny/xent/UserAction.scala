package ru.agny.xent

import ru.agny.xent.UserType._
import ru.agny.xent.core.utils.BuildingTemplate
import ru.agny.xent.core.{Building, LocalCell}

trait UserAction extends Action {
  type T = User

  override def run(user: T): Either[Response, T]
}

object DoNothing extends UserAction {
  override def run(user: User): Either[Response, User] = Right(user)
}

case class Idle(user: UserId) extends UserAction {
  override def run(user: User): Either[Response, User] = Right(user)
}

case class PlaceBuilding(facility: String, layer: Layer, cell: LocalCell) extends UserAction {
  override def run(user: User): Either[Response, User] = {
    val bt = layer.facilities.collectFirst { case bt: BuildingTemplate if bt.name == facility => bt }
    bt.map(x => {
      val shape = x.shape.form(cell)
      if (user.city.isEnoughSpace(shape))
        user.spend(x) match {
          case Left(v) => Left(v)
          case Right(v) => Right(v.build(LocalCell(cell.x, cell.y, Some(Building(x.name, x.resources, x.buildTime, shape)))))
        }
      else Left(Response(s"${x.name} can't be placed in $cell"))
    }) getOrElse Left(Response(s"Unable to build $facility"))
  }
}

case class AddProduction(facility: String, res: ResourceUnit) extends UserAction {
  override def run(user: User): Either[Response, User] = {
    user.findFacility(facility) match {
      case Some(v) => user.addProduction(v, res) match {
        case Left(l) => Left(l)
        case Right(r) => Right(user.copy(storage = r))
      }
      case None => Left(Response(s"Unable to find $facility"))
    }
  }
}
