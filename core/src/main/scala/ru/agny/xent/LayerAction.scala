package ru.agny.xent

import ru.agny.xent.UserType._
import ru.agny.xent.core.{Cell, Outpost}

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
        (layer.users.find(x => x.id == userId) match {
          case Some(u) => facilityT.map(y => Outpost(y.id, y.name, x.resource.get, y.obtainables, y.buildTime)) match {
            case Some(outpost) => u.spend(facilityT.get.cost) match {
              case Left(l) => Left(l)
              case Right(r) => Right((r, outpost))
            }
            case None => Left(Response(s"Unable to claim resource in $cell by $facilityName"))
          }
          case None => Left(Response(s"User with id=$userId isn't found in this layer"))
        }) match {
          case Left(v) => Left(v)
          case Right((user, building)) =>
            val updatedCell = x.copy(owner = Some(user.id), building = Some(building))
            val updatedUser = user.build(updatedCell)
            val updatedLayer = layer.copy(users = updatedUser +: layer.users.filterNot(_.id == updatedUser.id))
            Right(updatedLayer.updateMap(updatedCell))
        }
      case Some(x) if x.owner.nonEmpty => Left(Response(s"$cell is already claimed"))
      case Some(x) => Left(Response(s"$cell doesn't have a resource"))
      case None => Left(Response(s"Unable to find $cell"))
    }
  }
}

//TODO address city coordinates
case class NewUser(id: UserId, name: String) extends LayerAction {
  override def run(layer: Layer): Either[Response, Layer] = Right(layer.copy(users = User(id, name, City.empty(0, 0)) +: layer.users))
}
