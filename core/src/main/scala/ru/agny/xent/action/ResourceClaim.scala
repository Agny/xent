package ru.agny.xent.action

import ru.agny.xent.battle.Outpost
import ru.agny.xent.core.inventory.Extractable
import ru.agny.xent.core.utils.{ErrorCode, FacilityTemplate, TemplateProvider}
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.core.{Coordinate, Layer, ResourceCell, User}
import ru.agny.xent.messages.{ReactiveLog, ResponseOk}

case class ResourceClaim(facilityName: String, userId: UserId, c: Coordinate, src: ReactiveLog) extends LayerAction {
  override def run(layer: Layer): Layer = {
    val facilityT = TemplateProvider.get(src.layer, facilityName)
    val res = layer.map.find(c) match {
      case Some(x: ResourceCell) =>
        for {
          user <- layer.getUser(userId)
          outpost <- createFromTemplate(x.c, user, x.resource, facilityT)
          updatedCell <- Right(outpost)
          updatedUser <- user.build(updatedCell, facilityT.get.cost)
        } yield {
          val updatedLayer = layer.copy(users = updatedUser +: layer.users.filterNot(_.id == user.id))
          updatedLayer.updateMap(updatedCell)
        }
      case Some(_) => Left(ErrorCode.RESOURCE_CANT_BE_CLAIMED)
      case None => Left(ErrorCode.PLACE_NOT_FOUND)
    }
    res match {
      case Left(v) => src.failed(v); layer
      case Right(v) => src.respond(ResponseOk); v
    }
  }

  private def createFromTemplate(c: Coordinate, owner: User, res: Extractable, mbTemplate: Option[FacilityTemplate]): Either[ErrorCode.Value, Outpost] = {
    mbTemplate.map(x =>
      Right(Outpost(c, owner, x.name, res, x.obtainables, x.buildTime))
    ) getOrElse Left(ErrorCode.RESOURCE_CANT_BE_CLAIMED)
  }
}
