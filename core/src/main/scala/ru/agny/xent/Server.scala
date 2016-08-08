package ru.agny.xent

import java.util.concurrent.atomic.AtomicLong

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.{ResourceUnit, LocalCell, WorldCell}
import ru.agny.xent.utils.IdGen

class Server(layers:List[Layer], queue: MessageQueue) {
  val idGen = IdGen()
  val last: AtomicLong = new AtomicLong(0)

  val state = LayerRuntime.run(layers, queue)

  def newUser(name: String, layer: String): Response = {
    queue.push(NewUserMessage(idGen.next, name, layer), last.incrementAndGet())
    ResponseOk
  }

  def layerUp(user: UserId, layerFrom: String, layerTo: String): Response = {
    queue.push(LayerUpMessage(user, layerFrom, layerTo), last.incrementAndGet())
    ResponseOk
  }

  def claimResource(user: UserId, layer: String, facility: String, cell: WorldCell): Response = {
    queue.push(ResourceClaimMessage(user, layer, facility, cell), last.incrementAndGet())
    ResponseOk
  }

  def constructBuilding(user: UserId, layer: String, facility: String, cell: LocalCell): Response = {
    queue.push(BuildingConstructionMessage(user,layer, facility, cell), last.incrementAndGet())
    ResponseOk
  }

  def produce(user: UserId, layer:String, facility:String, res: ResourceUnit): Response  = {
    queue.push(AddProductionMessage(user, layer, facility, res), last.incrementAndGet())
    ResponseOk
  }

  def emptyMessage(user: UserId, layer: String): Response = {
    queue.push(EmptyMessage(user, layer), last.incrementAndGet())
    ResponseOk
  }
}
