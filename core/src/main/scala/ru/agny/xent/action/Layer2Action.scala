package ru.agny.xent.action

import ru.agny.xent.core.Layer
import ru.agny.xent.messages.Response

trait Layer2Action extends Action {
  type T = (Layer, Layer)

  override def run(layers: T): Either[Response, T]
}
