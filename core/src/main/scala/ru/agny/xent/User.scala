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

  def work(msg: Message, a: UserAction): User = {
    val (user, resp) = a.run(User(id, name, storage.tick(lastAction)))
    msg.reply(resp)
    user
  }

  override def toString = s"id=$id name=$name time=$lastAction"
}

object User {
  def apply(id: UserId, name: String): User = User(id, name, Storage.empty(), System.currentTimeMillis())

  def apply(id: UserId, name: String, storage: Storage): User = User(id, name, storage, System.currentTimeMillis())
}
