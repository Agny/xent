package ru.agny.xent

object LayerRuntime {
  private var newUsers: Map[User, String] = Map.empty
  //TODO concurrency?
  private var lastState: List[Layer] = List.empty
  private var actions: List[(ActionResult, Action)] = List.empty

  private def run(initialState:List[Layer]): List[Layer] = {
    lastState = initialState

    def handleUsersMigration(): List[Layer] = {
      val tmpUsers = newUsers
      val updatedLayers = lastState.map(x => x.join(tmpUsers.filter(u => u._2 == x.id).keys.toSeq))
      if (tmpUsers != newUsers) newUsers = newUsers -- tmpUsers.keys
      else newUsers = Map.empty
      updatedLayers
    }

    def handleUsersActions(): List[(ActionResult, Action)] = {
      val tmpActions = actions
      actions = List.empty
      tmpActions
    }

    new Thread(new Runnable {
      override def run(): Unit = {
        while (true) {
          lastState = handleUsersMigration().map(r => r.tick(handleUsersActions()))
          Thread.sleep(1000)
        }
      }
    }).start()

    lastState
  }

  def get = lastState

  def join(user: User, layer: String): Map[User, String] = {
    newUsers = newUsers + (user -> layer)
    println("users joined " + newUsers)
    newUsers
  }

  def queue(ar: (ActionResult, Action)): List[(ActionResult, Action)] = {
    actions = actions :+ ar
    println("actions committed"  + actions)
    actions
  }

  run(Server.layers)

}
