package ru.agny.xent

// stub front-end
case class Front(layers:LayerProvider) {
  def layer(name: String): Option[Layer] = layers.provide(name)
}

case class LayerProvider(layers: List[Layer]) {
  def provide(name: String): Option[Layer] = layers.find(l => l.id == name)
}

case class Server() {
  def createFront = Front(LayerProvider(LayerGenerator.setupLayers()))
}
