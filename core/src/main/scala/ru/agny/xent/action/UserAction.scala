package ru.agny.xent.action

import ru.agny.xent.core.User
import ru.agny.xent.messages.ActiveMessage

trait UserAction extends Action[ActiveMessage] {
  type T = User

  override def run(user: T): T
}

object DoNothing extends UserAction {
  override val src = ???

  override def run(user: User) = user
}