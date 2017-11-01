package ru.agny.xent.action

import ru.agny.xent.core.Layer
import ru.agny.xent.messages.ActiveMessage

trait Layer2Action extends Action[ActiveMessage] {
  type T = (Layer, Layer)

  override def run(layers: T): T
}
