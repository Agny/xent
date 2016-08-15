package ru.agny.xent

import ru.agny.xent.UserType._
import ru.agny.xent.core.{Cell, Building, LocalCell}

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

case class PlaceBuilding(facility: String, layer: Layer, cell: Cell) extends UserAction {
  override def run(user: User): Either[Response, User] = {
    layer.facilities.find(ft => ft.name == facility) match {
      case Some(ft) => user.city.find(cell) match {
        case Some(lc) if lc.building.nonEmpty => Left(Response(s"Cell $cell already contains building"))
        case Some(lc) => user.spend(ft) match {
          case Left(v) => Left(v)
          case Right(v) => Right(v.addBuilding(LocalCell(lc.x,lc.y,Some(Building(ft.name,ft.resources)))))
        }
        case None => Left(Response(s"Cell $cell doesn't exist"))
      }
      case None => Left(Response(s"Unable to build $facility"))
    }
  }
}

case class AddProduction(facility: String, res: ResourceUnit) extends UserAction {
  override def run(user: User): Either[Response, User] = {
    user.findFacility(facility) match {
      case Some(v) => user.addToQueue(v, res) match {
        case Left(l) => Left(l)
        case Right(r) => Right(user.copy(storage = r))
      }
      case None => Left(Response(s"Unable to find $facility"))
    }
  }
}
