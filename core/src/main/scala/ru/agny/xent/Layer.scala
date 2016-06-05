package ru.agny.xent

case class Layer(id: String, level: Int, users: Seq[User], cells: List[WorldCell], facilities: List[FacilityTemplate]) {

  def tick(actions: Seq[Action]): Layer = {
    val updatedUsers = users.map(x => x.tick(actions))
    val updatedResources = updatedUsers.flatMap(x => x.storage.resources)
    println(updatedResources + " -- " + users)
    Layer(id, level, updatedUsers, cells, facilities)
  }

  def join(added: Seq[User]): Layer = {
    Layer(id, level, users ++ added, cells, facilities)
  }

  def resourceClaim(user: User, facilityName: String, resourceId: Long): Either[Error, User] = {
    val resource = cells.find(x => x.resource.exists(y => y.id == resourceId))
    val facilityT = facilities.find(x => x.name == facilityName)
    resource.map(x => user.storage.findOutpost(x.resource.get) match {
      case Some(v) => Left(Error(s"Resource with id=$resourceId is already claimed"))
      case None => facilityT.map(y => Outpost(user.localIdGen.next, y.name, x.resource.get, y.cost)) match {
        case Some(v) => Right(User(user.id, user.name, Storage(user.storage.resources, v :: user.storage.producers), user.lastAction)) //TODO update user in layer
        case None => Left(Error(s"Unable to claim resource with id=$resourceId by $facilityName"))
      }
    }) match {
      case Some(v) => v
      case None => Left(Error(s"Resource with id=$resourceId doesn't exists on this layer"))
    }
  }
}
