package ru.agny.xent.trade

import ru.agny.xent.messages.Response
import ru.agny.xent.web.WSMessage

import scala.concurrent.Future

trait WSRequest {
  val in: WSMessage
  val out: Future[Response]
}
