package ru.agny.xent

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

  def claimResource(user: User, layer: String, facility: String, resourceId: Long) = layerProvider.provide(layer).map(x => x.resourceClaim(user, facility, resourceId))
}
