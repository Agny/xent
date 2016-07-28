package ru.agny.xent

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
            layers.find(l => l.id == x.layer) match {
              case Some(layer) => layer.tick(NewUser(x.user, x.name)) match {
                case Left(v) => x.reply(v); layers
                case Right(v) => v :: layers.diff(Seq(layer))
              }
              case None => x.reply(Response(s"Layer[${x.layer}] isn't found")); layers
            }
          case x: LayerUpMessage =>
            val (active, idle) = layers.partition(l => l.id == x.layerFrom || l.id == x.layerTo)
            val from = active.find(l => l.id == x.layerFrom).get
            val to = active.find(l => l.id == x.layerTo).get
            LayerChange(x.user).run(from, to) match {
              case Left(v) => x.reply(v); layers
              case Right(v) => List(v._1, v._2) ::: idle
            }
          case x: ResourceClaimMessage =>
            layers.find(l => l.id == x.layer) match {
              case Some(layer) => layer.tick(ResourceClaim(x.facility, x.user, x.cell)) match {
                case Left(v) => x.reply(v); layers
                case Right(v) => v :: layers.diff(Seq(layer))
              }
              case None => x.reply(Response(s"Layer[${x.layer}] isn't found")); layers
            }
          case x: BuildingConstructionMessage =>
            layers.find(l => l.id == x.layer) match {
              case Some(layer) => layer.tick(PlaceBuilding(x.building,layer,x.cell), x.user) match {
                case Left(v) => x.reply(v); layers
                case Right(v) => v :: layers.diff(Seq(layer))
              }
              case None => x.reply(Response(s"Layer[${x.layer}] isn't found")); layers
            }
          case x: EmptyMessage =>
            layers.find(l => l.id == x.layer) match {
              case Some(layer) => layer.tick(Idle(x.user), x.user) match {
                case Left(v) => x.reply(v); layers
                case Right(v) => v :: layers.diff(Seq(layer))
              }
              case None => x.reply(Response(s"Layer[${x.layer}] isn't found")); layers
            }
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
