package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.{WorldCell, Storage, Outpost}

trait Action {
  type T

  def run(e: T): (T, Response)
}

trait UserAction extends Action {
  type T = User

  override def run(user: T): (T, Response)
}

trait LayerAction extends Action {
  type T = Layer

  override def run(layer: T): (T, Response)
}

trait Layer2Action extends Action {
  type T = (Layer, Layer)

  override def run(layers: T): (T, Response)
}

object DoNothing extends UserAction {
  override def run(user: User): (User, Response) = (user, ResponseOk)
}

case class ResourceClaim(facilityName: String, layer: Layer, cell: WorldCell) extends UserAction {
  override def run(user: User): (User, Response) = {
    val facilityT = layer.facilities.find(x => x.name == facilityName)
    val resource = layer.map.find(cell)
    (resource match {
      case Some(x) if x.resource.nonEmpty && x.owner.isEmpty =>
        facilityT.map(y => Outpost(user.localIdGen.next, y.name, x.resource.get, y.cost)) match {
          case Some(v) => Right(User(user.id, user.name, Storage(user.storage.resources, v :: user.storage.producers), user.lastAction))
          case None => Left(Response(s"Unable to claim resource in [${cell.x},${cell.y}] by $facilityName"))
        }
      case Some(x) if x.owner.nonEmpty => Left(Response(s"[${cell.x},${cell.y}] is already claimed"))
      case Some(x) => Left(Response(s"[${cell.x},${cell.y}] doesn't have a resource"))
      case None => Left(Response(s"Unable to find [${cell.x},${cell.y}]"))
    }) match {
      case Left(v) => (user, v)
      case Right(r) => (r, ResponseOk)
    }
  }
}

case class NewUser(id:UserId, name:String) extends LayerAction {
  override def run(layer: Layer): (Layer, Response) = (layer.copy(users = layer.users :+ User(id,name)), ResponseOk)
}

case class LayerChange(user: UserId) extends Layer2Action {
  override def run(layers: (Layer, Layer)): ((Layer, Layer), Response) = {
    val from = layers._1
    val to = layers._2
    (from.users.find(x => x.id == user) match {
      case Some(v) =>
        val fromUsers = from.users.diff(Seq(user))
        val toUsers = to.users :+ v.work(EmptyMessage(v.id), DoNothing)
        Right((Layer(from.id, from.level, fromUsers, from.map, from.facilities), Layer(to.id, to.level, toUsers, to.map, to.facilities)))
      case None => Left(Response(s"There is no user with id[$user] in the layer[${from.id}]"))
    }) match {
      case Left(v) => (layers, v)
      case Right(r) => (r, ResponseOk)
    }
  }
}