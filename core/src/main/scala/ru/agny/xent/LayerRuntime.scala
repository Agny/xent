package ru.agny.xent

object LayerRuntime {

  private var newUsers: Map[User, String] = Map.empty //TODO concurrency?

  def run(layers: List[Layer], actions: Map[Long, Action]): List[Layer] = {
    //TODO have to send acquired message from server to make sure the operation was success
    def handleUsersMigration(): List[Layer] = {
      val tmpUsers = newUsers
      val updatedLayers = layers.map(x => x.join(tmpUsers.filter(u => u._2 == x.id).keys.toSeq))
      if (tmpUsers != newUsers) newUsers = newUsers -- tmpUsers.keys
      else newUsers = Map.empty
      updatedLayers
    }
    handleUsersMigration().map(r => r.tick(actions))
  }

  def join(user: User, layer: String): Map[User, String] = {
    newUsers = newUsers + (user -> layer)
    newUsers
  }

}
