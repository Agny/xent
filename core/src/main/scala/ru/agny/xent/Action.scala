package ru.agny.xent

import ru.agny.xent.core.{Storage, Outpost}

trait Action {
  def run(user: User): User
}

object DoNothing extends Action {
  override def run(user: User): User = user
}

case class ResourceClaim(facilityName: String, layer: Layer, resourceId: Long) extends Action {
  override def run(user: User): User = {
    val resource = layer.cells.find(x => x.resource.exists(y => y.id == resourceId))
    val facilityT = layer.facilities.find(x => x.name == facilityName)
    resource.map(x => user.storage.findOutpost(x.resource.get) match {
      case Some(v) => Left(Error(s"Resource with id=$resourceId is already claimed"))
      case None => facilityT.map(y => Outpost(user.localIdGen.next, y.name, x.resource.get, y.cost)) match {
        case Some(v) => Right(User(user.id, user.name, Storage(user.storage.resources, v :: user.storage.producers), user.lastAction))
        case None => Left(Error(s"Unable to claim resource with id=$resourceId by $facilityName"))
      }
    }) match {
      case Some(v) => v match {
        case Left(l) => println("Error " + l);user //subscriber notify error
        case Right(r) => println("Success " + r);r //subscriber notify success
      }
      case None => println(Left(Error(s"Resource with id=$resourceId doesn't exists on this layer")));user //subscriber notify error
    }
  }
}