package ru.agny.xent.action

import ru.agny.xent.core.Layer
import ru.agny.xent.core.utils.ErrorCode
import ru.agny.xent.core.utils.UserType.UserId
import ru.agny.xent.messages.{ReactiveLog, ResponseOk}

case class LayerChange(user: UserId, src: ReactiveLog) extends Layer2Action {
  override def run(layers: (Layer, Layer)): (Layer, Layer) = {
    val (from, to) = layers
    val (moving, staying) = from.users.partition(x => x.id == user)
    moving match {
      case h +: _ =>
        val updated = h.work(DoNothing)
        val toUsers = updated +: to.users
        src.respond(ResponseOk)
        (from.copy(users = staying), to.copy(users = toUsers))
      case _ => src.failed(ErrorCode.USER_NOT_FOUND); layers
    }
  }
}
