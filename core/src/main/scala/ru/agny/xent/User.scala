package ru.agny.xent

import ru.agny.xent.core.Storage
import ru.agny.xent.utils.IdGen

case class User(id: Long, name: String, storage: Storage, lastAction: Long) {
  val localIdGen = IdGen()
  //  implicit var rates: Map[Facility, Int] = Map(() -> 1) //very stronk formula from current layer/science/etc

  def work(action: Action): User = {
    action.run(User(id, name, storage.tick(lastAction), System.currentTimeMillis()))
  }

  override def toString = s"id=$id name=$name time=$lastAction"
}
