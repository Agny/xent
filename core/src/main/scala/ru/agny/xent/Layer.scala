package ru.agny.xent

case class Layer(id: String, level: Int, resources: List[Extractable], facilities: List[FacilityTemplate]) {

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

  def resourceClaim(user: User, facilityName: String, resourceId: Long): Either[Error, User] = {
    val resource = resources.find(x => x.id == resourceId)
    val facilityT = facilities.find(x => x.name == facilityName)
    resource.map(x => user.storage.findOutpost(x) match {
      case Some(v) => Left(Error(s"Resource with id=$resourceId is already claimed"))
      case None => facilityT.map(y => Outpost(user.localIdGen.next, y.name, x, y.recipe)) match {
        case Some(v) => Right(User(user.name, Storage(user.storage.resources, v :: user.storage.producers)))
        case None => Left(Error(s"Unable to claim resource with id=$resourceId by $facilityName"))
      }
    }) match {
      case Some(v) => v
      case None => Left(Error(s"Resource with id=$resourceId doesn't exists on this layer"))
    }
  }

  private def rates(f: Facility): Int = {
    f match {
      case x => 1
    }
  }

}
