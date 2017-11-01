package ru.agny.xent.action

import ru.agny.xent.core.Layer
import ru.agny.xent.messages.ActiveMessage

trait LayerAction extends Action[ActiveMessage] {
  type T = Layer

  override def run(layer: T): T
}