package ru.agny.xent.action

import ru.agny.xent.messages.Responder

trait Action[+R <: Responder] {
  type T
  val src: R

  def run(e: T): T
}