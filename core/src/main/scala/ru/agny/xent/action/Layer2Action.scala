package ru.agny.xent.action

import ru.agny.xent.core.UserType._
import ru.agny.xent.messages.Response

trait Layer2Action extends Action {
  type T = (Layer, Layer)

  override def run(layers: T): Either[Response, T]
}

case class LayerChange(user: UserId) extends Layer2Action {
  override def run(layers: (Layer, Layer)): Either[Response, (Layer, Layer)] = {
    val (from, to) = layers
    from.users.find(x => x.id == user) match {
      case Some(v) =>
        val fromUsers = from.users.diff(Vector(user))
        v.work(DoNothing) match {
          case Left(cantbe) => Left(cantbe)
          case Right(moving) =>
            val toUsers = moving +: to.users
            Right((from.copy(users = fromUsers), to.copy(users = toUsers)))
        }
      case None => Left(Response(s"There is no user with id[$user] in the layer[${from.id}]"))
    }
  }
}
