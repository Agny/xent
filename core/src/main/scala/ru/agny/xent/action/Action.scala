package ru.agny.xent.action

import ru.agny.xent.messages.PlainResponse

trait Action {
  type T

  def run(e: T): Either[PlainResponse, T]
}