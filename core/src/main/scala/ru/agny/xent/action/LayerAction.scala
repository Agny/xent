package ru.agny.xent.action

import ru.agny.xent.core.Layer
import ru.agny.xent.messages.Response

trait LayerAction extends Action {
  type T = Layer

  override def run(layer: T): Either[Response, T]
}