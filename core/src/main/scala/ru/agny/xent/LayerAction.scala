package ru.agny.xent

import ru.agny.xent.UserType._
import ru.agny.xent.core.utils.{FacilityTemplate, OutpostTemplate}
import ru.agny.xent.core.{Extractable, WorldCell, Cell, Outpost}

trait LayerAction extends Action {
  type T = Layer

  override def run(layer: T): Either[Response, T]
}

case class ResourceClaim(facilityName: String, userId: UserId, cell: Cell) extends LayerAction {
  override def run(layer: Layer): Either[Response, Layer] = {
    val facilityT = layer.facilities.find(x => x.name == facilityName)
    val resource = layer.map.find(cell)
    resource match {
      case Some(x) if x.resource.nonEmpty && x.owner.isEmpty =>
        for {
          user <- findUser(userId, layer.users)
          outpost <- createFromTemplate(facilityName, x.resource.get, layer.facilities)
          updatedCell <- Right(x.copy(owner = Some(user.id), building = Some(outpost)))
          updatedUser <- user.build(updatedCell, facilityT.get.cost)
        } yield {
          val updatedLayer = layer.copy(users = updatedUser +: layer.users.filterNot(_.id == user.id))
          updatedLayer.updateMap(updatedCell)
        }
      case Some(x) if x.owner.nonEmpty => Left(Response(s"$cell is already claimed"))
      case Some(x) => Left(Response(s"$cell doesn't have a resource"))
      case None => Left(Response(s"Unable to find $cell"))
    }
  }

  private def findUser(id: UserId, users: Vector[User]): Either[Response, User] = {
    users.find(x => x.id == userId).map(Right(_)) getOrElse Left(Response(s"User with id=$userId isn't found in this layer"))
  }

  private def createFromTemplate(name: String, res: Extractable, templates: Vector[FacilityTemplate]): Either[Response, Outpost] = {
    templates.find(_.name == name).map(x =>
      Right(Outpost(x.name, res, x.obtainables, x.buildTime))
    ) getOrElse Left(Response(s"Unable to claim resource in $cell by $name"))
  }
}

//TODO address city coordinates
case class NewUser(id: UserId, name: String) extends LayerAction {
  override def run(layer: Layer): Either[Response, Layer] = Right(layer.copy(users = User(id, name, City.empty(0, 0)) +: layer.users))
}
