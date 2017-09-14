package ru.agny.xent.action

import ru.agny.xent.messages.Response

trait Action {
  type T

  def run(e: T): Either[Response, T]
}