package ru.agny.xent

import ru.agny.xent.core.Storage
import ru.agny.xent.core.utils.LayerGenerator

object LayerRuntime {
  private val init = LayerGenerator.setupLayers()
  //TODO concurrency?
  private var lastState: List[Layer] = List.empty
  private var messages: List[Message] = List.empty

  private def run(initialState: List[Layer]): List[Layer] = {
    def handleMessages(): List[Message] = {
      val tmpMessages = messages
      messages = List.empty
      tmpMessages
    }

    new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          val actions = handleMessages().map {
            case x: NewUserMessage =>
              val (_, idle) = initialState.span(l => l.id == x.layer)
              val layerTo = initialState.find(l => l.id == x.layer).get
              val (to, _) = layerTo.join(User(x.user, x.name, Storage.empty(), System.currentTimeMillis()))
              to :: idle
            case x: LayerUpMessage =>
              val (_, idle) = initialState.span(l => l.id == x.layerFrom || l.id == x.layerTo)
              val layerFrom = initialState.find(l => l.id == x.layerFrom).get
              val layerTo = initialState.find(l => l.id == x.layerTo).get
              val (from, left) = layerFrom.leave(x.user)
              val (to, joined) = layerTo.join(left)
              from :: to :: idle
            case x: ResourceClaimMessage =>
              val (active, idle) = initialState.span(l => l.id == x.layer)
              active.map(y => y.tick((x, ResourceClaim(x.facility, y, x.resourceId))))
            case _ => initialState
          }
          lastState = actions.flatten
          Thread.sleep(1000)
        }
      }
    }).start()

    lastState
  }

  def get = lastState

  def queue(msg: Message): List[Message] = {
    messages = messages :+ msg
    println("messages committed" + messages)
    messages
  }

  run(init)

}
