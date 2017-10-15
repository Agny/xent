package ru.agny.xent.action

import ru.agny.xent.battle.Outpost
import ru.agny.xent.core.inventory.Extractable
import ru.agny.xent.core.utils.FacilityTemplate
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.core.{Coordinate, Layer, ResourceCell, User}
import ru.agny.xent.messages.Response

case class ResourceClaim(facilityName: String, userId: UserId, c: Coordinate) extends LayerAction {
  override def run(layer: Layer): Either[Response, Layer] = {
    val facilityT = layer.facilities.find(x => x.name == facilityName)
    val resource = layer.map.find(c)
    resource match {
      case Some(x: ResourceCell) =>
        for {
          user <- layer.getUser(userId)
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

  private def createFromTemplate(c: Coordinate, owner: User, name: String, res: Extractable, templates: Vector[FacilityTemplate]): Either[Response, Outpost] = {
    templates.find(_.name == name).map(x =>
      Right(Outpost(c, owner, x.name, res, x.obtainables, x.buildTime))
    ) getOrElse Left(Response(s"Unable to claim resource in $c by $name"))
  }
}
