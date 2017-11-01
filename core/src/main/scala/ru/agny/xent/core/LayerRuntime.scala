package ru.agny.xent.core

import ru.agny.xent.action.{LayerAction, UserAction}
import ru.agny.xent.core.utils.ErrorCode
import ru.agny.xent.messages.{ActiveMessage, LayerUpMessage, Message, MessageQueue}

class LayerRuntime(queue: MessageQueue[Message]) {
  private var lastState: Vector[Layer] = Vector.empty

  private def run(initialState: Vector[Layer]): Vector[Layer] = {
    lastState = initialState
    new Thread(() => {
      while (true) {
        lastState = handle(lastState, queue.take())
        Thread.sleep(1000)
      }
    }).start()

    lastState
  }

  private def handle(startState: Vector[Layer], messages: Vector[Message]): Vector[Layer] = {
    messages.foldLeft(startState)((layers, m) => runAction(m, layers))
  }

  private def runAction(msg: Message, layers: Vector[Layer]): Vector[Layer] = {
    msg match {
      case specialCase: LayerUpMessage =>
        val (active, idle) = layers.partition(l => l.id == specialCase.layer || l.id == specialCase.layerTo)
        val from = active.find(l => l.id == specialCase.layer).get
        val to = active.find(l => l.id == specialCase.layerTo).get
        val (updatedFrom, updatedTo) = specialCase.action.run(from, to)
        Vector(updatedFrom, updatedTo) ++ idle
      case a: ActiveMessage => a.action match {
        case x: UserAction =>
          layers.find(l => l.id == msg.layer) match {
            case Some(layer) =>
              val l = layer.tick(x, msg.user)
              if (l == layer) layers else l +: layers.diff(Vector(layer))
            case None => a.failed(ErrorCode.LAYER_NOT_FOUND); layers
          }
        case x: LayerAction =>
          layers.find(l => l.id == msg.layer) match {
            case Some(layer) =>
              val l = layer.tick(x)
              if (l == layer) layers else l +: layers.diff(Vector(layer))
            case None => a.failed(ErrorCode.LAYER_NOT_FOUND); layers
          }
      }
    }
  }

  def get = lastState
}

object LayerRuntime {
  def run(layers: Vector[Layer], queue: MessageQueue[Message]) = {
    val runtime = new LayerRuntime(queue)
    runtime.run(layers)
    runtime
  }
}
