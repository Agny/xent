package ru.agny.xent

import ru.agny.xent.core.Storage
import ru.agny.xent.core.utils.LayerGenerator

object LayerRuntime {
  private val init = LayerGenerator.setupLayers()
  //TODO concurrency?
  private var lastState: List[Layer] = List.empty
  private var messages: List[Message] = List.empty

  private def run(initialState: List[Layer]): List[Layer] = {
    lastState = initialState
    def handleMessages(): List[Message] = {
      val tmpMessages = messages
      messages = List.empty
      tmpMessages
    }

    new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          lastState = rec(lastState, handleMessages())
          Thread.sleep(1000)
        }
      }
    }).start()

    lastState
  }

  private def rec(layers: List[Layer], messages: List[Message]): List[Layer] = {
    messages match {
      case h :: t =>
        val rez = h match {
          case x: NewUserMessage =>
            val (_, idle) = layers.span(l => l.id == x.layer)
            val layerTo = layers.find(l => l.id == x.layer).get
            val (to, _) = layerTo.join(User(x.user, x.name, Storage.empty(), System.currentTimeMillis()))
            to :: idle
          case x: LayerUpMessage =>
            val (_, idle) = layers.span(l => l.id == x.layerFrom || l.id == x.layerTo)
            val layerFrom = layers.find(l => l.id == x.layerFrom).get
            val layerTo = layers.find(l => l.id == x.layerTo).get
            val (from, left) = layerFrom.leave(x.user)
            val (to, _) = layerTo.join(left)
            from :: to :: idle
          case x: ResourceClaimMessage =>
            val (active, idle) = layers.span(l => l.id == x.layer)
            active.map(y => y.tick((x, ResourceClaim(x.facility, y, x.resourceId)))) ::: idle
          case _ => layers
        }
        rec(rez, t)
      case Nil => layers
    }
  }

  def get = lastState

  def queue(msg: Message): List[Message] = {
    messages = messages :+ msg
    println("messages committed" + messages)
    messages
  }

  run(init)

}
