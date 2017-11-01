package ru.agny.xent.messages

import ru.agny.xent.core.utils.ErrorCode

import scala.concurrent.Future

trait Responder {
  type ResponseType <: Response
  def respond(value: ResponseType): Future[ResponseType]
  def failed(code: ErrorCode.Value)
}
