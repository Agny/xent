package ru.agny.xent

import ru.agny.xent.core.LayerGenerator

// stub front-end
case class Front(layers: LayerProvider) {
  def layer(name: String): Option[Layer] = layers.provide(name)
}

case class LayerProvider(layers: List[Layer]) {
  def provide(name: String): Option[Layer] = layers.find(l => l.id == name)
}

object Server {
  private val layerProvider = LayerProvider(LayerGenerator.setupLayers())
  private val front = Front(layerProvider)

  def claimResource(msg: ResourceClaimMessage): Option[ResourceClaim] = layerProvider.provide(msg.layer).map(x => ResourceClaim(msg.facility, x, msg.resourceId))

  def layers = layerProvider.layers
}
