package ru.agny.xent

import ru.agny.xent.core.{Storage, Outpost}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait Action {
  def run(user: User): Future[(User, Response)]
}

object DoNothing extends Action {
  override def run(user: User): Future[(User, Response)] = Future.successful((user, Response("Ok")))
}

case class ResourceClaim(facilityName: String, layer: Layer, resourceId: Long) extends Action {
  override def run(user: User): Future[(User, Response)] = Future {
    val resource = layer.cells.find(x => x.resource.exists(y => y.id == resourceId))
    val facilityT = layer.facilities.find(x => x.name == facilityName)
    resource.map(x => user.storage.findOutpost(x.resource.get) match {
      case Some(v) => Left(Response(s"Resource with id=$resourceId is already claimed"))
      case None => facilityT.map(y => Outpost(user.localIdGen.next, y.name, x.resource.get, y.cost)) match {
        case Some(v) => Right(User(user.id, user.name, Storage(user.storage.resources, v :: user.storage.producers), user.lastAction))
        case None => Left(Response(s"Unable to claim resource with id=$resourceId by $facilityName"))
      }
    }) match {
      case Some(v) => v match {
        case Left(l) => (user, l)
        case Right(r) => (r, Response("Ok"))
      }
      case None => (user, Response(s"Resource with id=$resourceId doesn't exists on this layer"))
    }
  }
}