package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.core.{CellsMap, WorldCell}
import ru.agny.xent.core.utils.FacilityTemplate

case class Layer(id: String, level: Int, users: Vector[User], troops: Set[Troop], map: CellsMap[WorldCell], facilities: Vector[FacilityTemplate]) {

  def tick(action: LayerAction): Either[Response, Layer] = {
    action.run(this)
  }

  def tick(action: UserAction, user: UserId): Either[Response, Layer] = {
    users.find(_.id == user) match {
      case Some(v) => v.work(action) match {
        case Left(x) => Left(x)
        case Right(x) => Right(this.copy(users = x +: users.diff(Vector(v))))
      }
      case None => Left(Response(s"User with id=$user isn't found in this layer"))
    }
  }

  def updateMap(cell: WorldCell): Layer = {
    Layer(id, level, users, troops, map.update(cell), facilities)
  }
}
