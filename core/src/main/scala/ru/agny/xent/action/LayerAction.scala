package ru.agny.xent.action

import ru.agny.xent.battle.Outpost
import ru.agny.xent.core.utils.UserType._
import ru.agny.xent.core.city.City
import ru.agny.xent.core.inventory.Extractable
import ru.agny.xent.core.utils.{FacilityTemplate, ItemIdGenerator}
import ru.agny.xent.core.{Coordinate, ResourceCell, User}
import ru.agny.xent.messages.Response

trait LayerAction extends Action {
  type T = Layer

  override def run(layer: T): Either[Response, T]
}

case class ResourceClaim(facilityName: String, userId: UserId, c: Coordinate) extends LayerAction {
  override def run(layer: Layer): Either[Response, Layer] = {
    val facilityT = layer.facilities.find(x => x.name == facilityName)
    val resource = layer.map.find(c)
    resource match {
      case Some(x: ResourceCell) =>
        for {
          user <- findUser(userId, layer.users)
          outpost <- createFromTemplate(x.c, user, facilityName, x.resource, layer.facilities)
          updatedCell <- Right(outpost)
          updatedUser <- user.build(updatedCell, facilityT.get.cost)
        } yield {
          val updatedLayer = layer.copy(users = updatedUser +: layer.users.filterNot(_.id == user.id))
          updatedLayer.updateMap(updatedCell)
        }
      case Some(_: Outpost) => Left(Response(s"$c is already claimed"))
      case Some(_) => Left(Response(s"$c doesn't have a resource"))
      case None => Left(Response(s"Unable to find $c"))
    }
  }

  private def findUser(id: UserId, users: Vector[User]): Either[Response, User] = {
    users.find(x => x.id == id).map(Right(_)) getOrElse Left(Response(s"User with id=$id isn't found in this layer"))
  }

  private def createFromTemplate(c: Coordinate, owner: User, name: String, res: Extractable, templates: Vector[FacilityTemplate]): Either[Response, Outpost] = {
    templates.find(_.name == name).map(x =>
      Right(Outpost(c, owner, x.name, res, x.obtainables, x.buildTime))
    ) getOrElse Left(Response(s"Unable to claim resource in $c by $name"))
  }
}

//TODO address city coordinates
case class NewUser(id: UserId, name: String) extends LayerAction {
  override def run(layer: Layer): Either[Response, Layer] = Right(layer.copy(users = User(id, name, City.empty(0, 0)) +: layer.users))
}

case class CreateTroop(id: UserId, souls: Vector[Long]) extends LayerAction {
  override def run(layer: Layer) = {
    for {
      user <- findUser(id, layer.users)
      userWithTroop <- user.createTroop(ItemIdGenerator.next, souls)
    } yield {
      val (updateUser, troop) = userWithTroop
      val updatedLayer = layer.copy(users = updateUser +: layer.users.filterNot(_.id == user.id))
      updatedLayer.addTroop(troop)
    }
  }

  private def findUser(id: UserId, users: Vector[User]): Either[Response, User] = {
    users.find(x => x.id == id).map(Right(_)) getOrElse Left(Response(s"User with id=$id isn't found in this layer"))
  }
}
