package ru.agny.xent

import ru.agny.xent.core.WorldCell
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
            //TODO need some kind of abstraction for this handling
          case x: NewUserMessage =>
            val layerTo = layers.find(l => l.id == x.layer).get
            val (updated, msg) = NewUser(x.user,x.name).run(layerTo)
            x.reply(msg)
            updated :: layers.diff(List(layerTo))
          case x: LayerUpMessage =>
            val (active, idle) = layers.partition(l => l.id == x.layerFrom || l.id == x.layerTo)
            val from = active.find(l => l.id == x.layerFrom).get
            val to = active.find(l => l.id == x.layerTo).get
            val (updated, msg) = LayerChange(x.user).run(from,to)
            x.reply(msg)
            List(updated._1, updated._2) ::: idle
          case x: ResourceClaimMessage =>
            val (active, idle) = layers.span(l => l.id == x.layer)
            active.map(y => {
              val layer = y.tick((x, ResourceClaim(x.facility, y, x.cell)))
              val cellToUpdate = y.map.find(x.cell).get
              layer.updateMap(cellToUpdate.copy(owner = Some(x.user)))
            }) ::: idle
          case _ => layers
        }
        rec(rez, t)
      case Nil => layers
    }
  }

  def get = lastState

  def queue(msg: Message): List[Message] = {
    messages = messages :+ msg
    println("messages committed " + messages)
    messages
  }

  run(init)

}
