package ru.agny.xent

class LayerRuntime(queue: MessageQueue) {
  private var lastState: List[Layer] = List.empty

  private def run(initialState: List[Layer]): List[Layer] = {
    lastState = initialState

    new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          lastState = rec(lastState, queue.take())
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
                case Right(v) => x.reply(ResponseOk);v :: layers.diff(Seq(layer))
              }
              case None => x.reply(Response(s"Layer[${x.layer}] isn't found")); layers
            }
          case x: LayerUpMessage =>
            val (active, idle) = layers.partition(l => l.id == x.layerFrom || l.id == x.layerTo)
            val from = active.find(l => l.id == x.layerFrom).get
            val to = active.find(l => l.id == x.layerTo).get
            LayerChange(x.user).run(from, to) match {
              case Left(v) => x.reply(v); layers
              case Right(v) => x.reply(ResponseOk);List(v._1, v._2) ::: idle
            }
          case x: ResourceClaimMessage =>
            layers.find(l => l.id == x.layer) match {
              case Some(layer) => layer.tick(ResourceClaim(x.facility, x.user, x.cell)) match {
                case Left(v) => x.reply(v); layers
                case Right(v) => x.reply(ResponseOk);v :: layers.diff(Seq(layer))
              }
              case None => x.reply(Response(s"Layer[${x.layer}] isn't found")); layers
            }
          case x: BuildingConstructionMessage =>
            layers.find(l => l.id == x.layer) match {
              case Some(layer) => layer.tick(PlaceBuilding(x.building, layer, x.cell), x.user) match {
                case Left(v) => x.reply(v); layers
                case Right(v) => x.reply(ResponseOk);v :: layers.diff(Seq(layer))
              }
              case None => x.reply(Response(s"Layer[${x.layer}] isn't found")); layers
            }
          case x: AddProductionMessage =>
            layers.find(l => l.id == x.layer) match {
              case Some(layer) => layer.tick(AddProduction(x.facility, x.res), x.user) match {
                case Left(v) => x.reply(v); layers
                case Right(v) => x.reply(ResponseOk);v :: layers.diff(Seq(layer))
              }
              case None => x.reply(Response(s"Layer[${x.layer}] isn't found")); layers
            }
          case x: EmptyMessage =>
            layers.find(l => l.id == x.layer) match {
              case Some(layer) => layer.tick(Idle(x.user), x.user) match {
                case Left(v) => x.reply(v); layers
                case Right(v) => x.reply(ResponseOk);v :: layers.diff(Seq(layer))
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
}

object LayerRuntime {
  def run(layers:List[Layer], queue: MessageQueue) = {
    val runtime = new LayerRuntime(queue)
    runtime.run(layers)
    runtime
  }
}
