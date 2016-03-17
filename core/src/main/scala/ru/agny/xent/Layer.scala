package ru.agny.xent

case class Layer(id:String, resources: List[Extractable]) {

  def tick(users: List[User], actions: Facility=>Resource): List[Resource] = {
    val updatedUsers = users.map(x => x.tick(rates, actions))
    val updatedResources = updatedUsers.flatMap(x => x.storage.resources)
    println(updatedResources)
    Thread.sleep(5000)
    tick(updatedUsers, actions)
  }

//  def claim(user: User, res: Extractable): Outpost = {
//    Outpost(user.idGen.next, res.name, res, Set.empty)
//  }

  def rates(f:Facility):Int = {
    f match {
      case x => 1
    }
  }

}
