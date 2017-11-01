package ru.agny.xent.core

import ru.agny.xent.action.{LayerAction, UserAction}
import ru.agny.xent.battle.unit.{Cargo, Troop}
import ru.agny.xent.battle.{MapObject, Military}
import ru.agny.xent.core.utils.ErrorCode
import ru.agny.xent.core.utils.UserType.UserId

case class Layer(id: String, level: Int, users: Vector[User], armies: Military, map: CellsMap) {

  def tick(action: LayerAction): Layer = {
    action.run(militaryTick())
  }

  def tick(action: UserAction, user: UserId): Layer = {
    getUser(user) match {
      case Left(v) => action.src.failed(v); this
      case Right(v) =>
        val updated = v.work(action)
        if (updated == v) this
        else militaryTick().copy(users = updated +: users.diff(Vector(v)))
    }
  }

  def updateMap(cell: Cell): Layer = {
    val armiesUpdated = cell match {
      case o: MapObject => armies.add(o)
      case _ => armies
    }
    Layer(id, level, users, armiesUpdated, map.update(cell))
  }

  def addTroop(t: Troop): Layer = {
    val updated = militaryTick()
    updated.copy(armies = updated.armies.add(t))
  }

  def getUser(id: UserId): Either[ErrorCode.Value, User] = {
    users.find(x => x.id == id).map(Right(_)) getOrElse Left(ErrorCode.USER_NOT_FOUND)
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
