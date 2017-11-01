package ru.agny.xent.messages

import ru.agny.xent.action.Action
import ru.agny.xent.messages

trait ActiveMessage extends Message with Responder {
  type ActionType = Action[messages.Responder]
  val action: ActionType
}
