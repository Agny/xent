package ru.agny.xent

case class Layer(id: String, resources: List[Extractable]) {

  val cells = Map[Pos, Cell]

  def generateMap(): Map[Pos, Cell] = {
    ???
  }

  def tick(users: List[User], actions: Facility => Resource): List[Resource] = {
    val updatedUsers = users.map(x => x.tick(rates, actions))
    val updatedResources = updatedUsers.flatMap(x => x.storage.resources)
    println(updatedResources)
    Thread.sleep(5000)
    tick(updatedUsers, actions)
  }

  def rates(f: Facility): Int = {
    f match {
      case x => 1
    }
  }

}
