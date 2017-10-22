package ru.agny.xent.messages

import ru.agny.xent.core.utils.UserType.UserId

trait Message {
  val user: UserId
  val layer: String
}
