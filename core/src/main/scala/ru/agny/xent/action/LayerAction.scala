package ru.agny.xent.action

import ru.agny.xent.core.Layer
import ru.agny.xent.messages.PlainResponse

trait LayerAction extends Action {
  type T = Layer

  override def run(layer: T): Either[PlainResponse, T]
}