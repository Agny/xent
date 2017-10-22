package ru.agny.xent.action

import ru.agny.xent.core.Layer
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.PlainResponse

case class LayerChange(user: UserId) extends Layer2Action {
  override def run(layers: (Layer, Layer)): Either[PlainResponse, (Layer, Layer)] = {
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
      case None => Left(PlainResponse(s"There is no user with id[$user] in the layer[${from.id}]"))
    }
  }
}
