package ru.agny.xent.action

import ru.agny.xent.core.User
import ru.agny.xent.messages.{ActiveMessage, EmptyMessage}

trait UserAction extends Action[ActiveMessage] {
  type T = User

  override def run(user: T): T
}

object DoNothing extends UserAction {
  override val src = EmptyMessage(-1, "")

  override def run(user: User) = user
}