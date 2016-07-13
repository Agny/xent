package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core.Storage
import ru.agny.xent.utils.IdGen

object UserType {
  type UserId = Long
}

case class User(id: UserId, name: String, city: City, storage: Storage, lastAction: Long) {
  val localIdGen = IdGen()

  def work(msg: Message, a: UserAction): User = {
    val (user, resp) = a.run(User(id, name, city, storage.tick(lastAction)))
    msg.reply(resp)
    user
  }

  override def toString = s"id=$id name=$name time=$lastAction"
}

object User {
  def apply(id: UserId, name: String, city: City): User = User(id, name, city, Storage.empty, System.currentTimeMillis())

  def apply(id: UserId, name: String, city: City, storage: Storage): User = User(id, name, city, storage, System.currentTimeMillis())
}
