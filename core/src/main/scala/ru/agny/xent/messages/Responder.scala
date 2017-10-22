package ru.agny.xent.messages

import scala.concurrent.Future

trait Responder[T <: Response[_]] {

  def respond(value: T): Future[T]

}
