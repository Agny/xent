package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.WorldCell
import ru.agny.xent.utils.IdGen

object Server {
  val idGen = IdGen()

  def newUser(name: String, layer: String): Response = {
    LayerRuntime.queue(NewUserMessage(idGen.next, name, layer))
    ResponseOk
  }

  def layerUp(user: UserId, layerFrom: String, layerTo: String): Response = {
    LayerRuntime.queue(LayerUpMessage(user, layerFrom, layerTo))
    ResponseOk
  }

  def claimResource(user: UserId, layer: String, facility: String, cell: WorldCell): Response = {
    LayerRuntime.queue(ResourceClaimMessage(user, layer, facility, cell))
    ResponseOk
  }

  def emptyMessage(user: UserId, layer: String): Response = {
    LayerRuntime.queue(EmptyMessage(user, layer))
    ResponseOk
  }
}
