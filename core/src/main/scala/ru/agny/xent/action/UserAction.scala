package ru.agny.xent.action

import ru.agny.xent.core.User
import ru.agny.xent.messages.PlainResponse

trait UserAction extends Action {
  type T = User

  override def run(user: T): Either[PlainResponse, T]
}

object DoNothing extends UserAction {
  override def run(user: User) = Right(user)
}