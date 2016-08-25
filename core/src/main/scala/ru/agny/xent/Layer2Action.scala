package ru.agny.xent

import ru.agny.xent.UserType._

trait Layer2Action extends Action {
  type T = (Layer, Layer)

  override def run(layers: T): Either[Response, T]
}

case class LayerChange(user: UserId) extends Layer2Action {
  override def run(layers: (Layer, Layer)): Either[Response, (Layer, Layer)] = {
    val from = layers._1
    val to = layers._2
    from.users.find(x => x.id == user) match {
      case Some(v) =>
        val fromUsers = from.users.diff(Seq(user))
        v.work(DoNothing) match {
          case Left(cantbe) => Left(cantbe)
          case Right(moving) =>
            val toUsers = to.users :+ moving
            Right((from.copy(users = fromUsers), to.copy(users = toUsers)))
        }
      case None => Left(Response(s"There is no user with id[$user] in the layer[${from.id}]"))
    }
  }
}
