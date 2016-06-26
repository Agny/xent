package ru.agny.xent

import ru.agny.xent.core.LayerGenerator

case class Front(layers: LayerProvider) {
  def layer(name: String): Option[Layer] = layers.provide(name)
}

case class LayerProvider(layers: List[Layer]) {
  def provide(name: String): Option[Layer] = layers.find(l => l.id == name)
}

object Server {
  private val layerProvider = LayerProvider(LayerGenerator.setupLayers())
  private val front = Front(layerProvider)

  def claimResource(user: User, layer: String, facility: String, resourceId: Long): Response = {
    val mbMsg = layerProvider.provide(layer).map(x => ResourceClaim(facility, x, resourceId))
    val msg = mbMsg.get
    LayerRuntime.queue((ActionResult(user.id), msg))
    Response("Ok")
  }

  def layers = layerProvider.layers
}
