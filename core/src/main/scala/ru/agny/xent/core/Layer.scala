package ru.agny.xent.core

import ru.agny.xent.action.{LayerAction, UserAction}
import ru.agny.xent.battle.unit.{Cargo, Troop}
import ru.agny.xent.battle.{MapObject, Military}
import ru.agny.xent.core.utils.FacilityTemplate
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.Response

case class Layer(id: String, level: Int, users: Vector[User], armies: Military, map: CellsMap, facilities: Vector[FacilityTemplate]) {

  def tick(action: LayerAction): Either[Response, Layer] = {
    action.run(militaryTick())
  }

  def tick(action: UserAction, user: UserId): Either[Response, Layer] = {
    getUser(user) match {
      case Right(v) => v.work(action) match {
        case Left(x) => Left(x)
        case Right(x) => Right(militaryTick().copy(users = x +: users.diff(Vector(v))))
      }
      case Left(v) => Left(v)
    }
  }

  def updateMap(cell: Cell): Layer = {
    val armiesUpdated = cell match {
      case o: MapObject => armies.add(o)
      case _ => armies
    }
    Layer(id, level, users, armiesUpdated, map.update(cell), facilities)
  }

  def addTroop(t: Troop): Layer = {
    val updated = militaryTick()
    updated.copy(armies = updated.armies.add(t))
  }

  def getUser(id: UserId): Either[Response, User] = {
    users.find(x => x.id == id).map(Right(_)) getOrElse Left(Response(s"User with id=$id isn't found in the layer ${this.id}"))
  }

  private def militaryTick(): Layer = {
    val (updated, quitters) = armies.tick()
    val updatedUsers = handleQuitters(quitters)
    val notUpdatedUsers = users.diff(updatedUsers)
    copy(users = notUpdatedUsers ++ updatedUsers, armies = updated)
  }

  private def handleQuitters(quitters: Vector[MapObject]) = {
    val empty = Map.empty[UserId, Vector[MapObject]].withDefaultValue(Vector.empty)
    val withUsers = quitters.foldLeft(empty)((acc, x) => acc.updated(x.id, x +: acc(x.id)))
    withUsers.foldLeft(Vector.empty[Option[User]]) {
      case (acc, (usrId, objs)) => users.find(_.id == usrId).map(u => objs.foldLeft(u)(consume)) +: acc
    }.flatten
  }

  private def consume(usr: User, v: MapObject) = v match {
    case v: Troop => if (v.isDiscardable) usr.assimilateTroop(v) else usr
    case v: Cargo => usr.copy(city = usr.city.addResources(v.resources))
    case _ => usr
  }
}
