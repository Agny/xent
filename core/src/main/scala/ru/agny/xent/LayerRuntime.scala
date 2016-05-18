package ru.agny.xent

object LayerRuntime {

  private var newUsers: Map[User, String] = Map.empty

  def run(layers: List[Layer]): List[Layer] = {
    //TODO have to send acquired message from server to make sure the operation was success
    def handleUsersMigration(): List[Layer] = {
      val tmpUsers = newUsers
      val updatedLayers = layers.map(x => x.join(tmpUsers.filter(u => u._2 == x.id).keys.toSeq))
      if (tmpUsers != newUsers) newUsers = newUsers -- tmpUsers.keys
      else newUsers = Map.empty
      updatedLayers
    }

    val nextState = handleUsersMigration().map(r => r.tick(Seq.empty))
    Thread.sleep(5000)
    run(nextState)
  }

  def join(user: User, layer: String): Map[User, String] = {
    newUsers = newUsers + (user -> layer)
    newUsers
  }

}
