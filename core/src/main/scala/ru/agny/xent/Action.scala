package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.{WorldCell, Storage, Outpost}

trait Action {
  type T

  def run(e: T): Either[Response, T]
}

trait UserAction extends Action {
  type T = User

  override def run(user: T): Either[Response, T]
}

trait LayerAction extends Action {
  type T = Layer

  override def run(layer: T): Either[Response, T]
}

trait Layer2Action extends Action {
  type T = (Layer, Layer)

  override def run(layers: T): Either[Response, T]
}

object DoNothing extends UserAction {
  override def run(user: User): Either[Response, User] = Right(user)
}

case class ResourceClaim(facilityName: String, userId: UserId, cell: WorldCell) extends LayerAction {
  override def run(layer: Layer): Either[Response, Layer] = {
    val facilityT = layer.facilities.find(x => x.name == facilityName)
    val resource = layer.map.find(cell)
    resource match {
      case Some(x) if x.resource.nonEmpty && x.owner.isEmpty =>
        (layer.users.find(x => x.id == userId) match {
          case Some(u) => facilityT.map(y => Outpost(u.localIdGen.next, y.name, x.resource.get, y.cost)) match {
            case Some(outpost) => u.spend(outpost) match {
              case Left(l) => Left(l)
              case Right(r) => Right(r.addFacility(outpost))
            }
            case None => Left(Response(s"Unable to claim resource in [${cell.x},${cell.y}] by $facilityName"))
          }
          case None => Left(Response(s"User with id=$userId isn't found in this layer"))
        }) match {
          case Left(v) => Left(v)
          case Right(v) =>
            val layerToUpdate = layer.copy(users = layer.users.diff(Seq(v)) :+ v)
            val cellToUpdate = layer.map.find(cell).get
            Right(layerToUpdate.updateMap(cellToUpdate.copy(owner = Some(v.id))))
        }
      case Some(x) if x.owner.nonEmpty => Left(Response(s"[${cell.x},${cell.y}] is already claimed"))
      case Some(x) => Left(Response(s"[${cell.x},${cell.y}] doesn't have a resource"))
      case None => Left(Response(s"Unable to find [${cell.x},${cell.y}]"))
    }
  }
}

case class NewUser(id: UserId, name: String) extends LayerAction {
  override def run(layer: Layer): Either[Response, Layer] = Right(layer.copy(users = layer.users :+ User(id, name, City.empty)))
}

case class LayerChange(user: UserId) extends Layer2Action {
  override def run(layers: (Layer, Layer)): Either[Response,(Layer, Layer)] = {
    val from = layers._1
    val to = layers._2
    from.users.find(x => x.id == user) match {
      case Some(v) =>
        val fromUsers = from.users.diff(Seq(user))
        v.work(DoNothing) match {
          case Left(cantbe) => Left(cantbe)
          case Right(moving) =>
            val toUsers = to.users :+ moving
            Right((Layer(from.id, from.level, fromUsers, from.map, from.facilities), Layer(to.id, to.level, toUsers, to.map, to.facilities)))
        }
      case None => Left(Response(s"There is no user with id[$user] in the layer[${from.id}]"))
    }
  }
}