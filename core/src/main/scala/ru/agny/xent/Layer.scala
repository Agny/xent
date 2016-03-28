package ru.agny.xent

case class Layer(id: String, level: Int, resources:List[Extractable], facilities:List[FacilityTemplate]) {

  import LayerGenerator._

  val size = 30
  val cells = generateMap()
  var users = List.empty

  private def generateMap(): List[WorldCell] = {
    def genByX(x: Int)(y: Int, acc: List[WorldCell]): List[WorldCell] = {
      if (x < size) genByX(x + 1)(y, WorldCell(x, y, mbResource(resources)) :: acc)
      else WorldCell(x, y, mbResource(resources)) :: acc
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

  def resourceClaim(user: User, facilityName: String, res: Extractable): Either[Error, User] = {
//    Outpost(user.localIdGen.next, facilityName, res,)
    ???
  }

  private def rates(f: Facility): Int = {
    f match {
      case x => 1
    }
  }

}
