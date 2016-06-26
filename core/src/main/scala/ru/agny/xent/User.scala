package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.Storage
import ru.agny.xent.utils.IdGen

object UserType {
  type UserId = Long
}

case class User(id: UserId, name: String, storage: Storage, lastAction: Long) {
  val localIdGen = IdGen()
  //  implicit var rates: Map[Facility, Int] = Map(() -> 1) //very stronk formula from current layer/science/etc

  def work(ar: ActionResult, a: Action): User = {
    val res = a.run(User(id, name, storage.tick(lastAction), System.currentTimeMillis()))
    ar.send(res)
  }

  override def toString = s"id=$id name=$name time=$lastAction"
}
