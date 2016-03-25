package ru.agny.xent

case class Layer(id: String, level: Int) {

  import ResourceGenerator._

  val size = 30
  val resourceTemplates = gen(level)
  val cells = generateMap()

  private def generateMap(): List[WorldCell] = {
    def genByX(x: Int)(y: Int, acc: List[WorldCell]): List[WorldCell] = {
      if (x < size) genByX(x + 1)(y, WorldCell(x, y, mbResource(resourceTemplates)) :: acc)
      else  WorldCell(x, y, mbResource(resourceTemplates)) :: acc
    }

    (0 to size).flatMap(y => genByX(0)(y, List.empty)).toList
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
