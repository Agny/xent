package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.{CellsMap, WorldCell}
import ru.agny.xent.core.utils.FacilityTemplate

case class Layer(id: String, level: Int, users: Seq[User], map: CellsMap[WorldCell], facilities: List[FacilityTemplate]) {

  def tick(action: LayerAction): Either[Response, Layer] = {
    action.run(this)
  }

  def tick(action: UserAction, user: UserId): Either[Response, Layer] = {
    users.find(_.id == user) match {
      case Some(v) => v.work(action) match {
        case Left(x) => Left(x)
        case Right(x) => Right(this.copy(users = users.diff(List(v)) :+ x))
      }
      case None => Left(Response(s"User with id=$user isn't found in this layer"))
    }
  }

  def updateMap(cell: WorldCell): Layer = {
    Layer(id, level, users, map.update(cell), facilities)
  }
}
