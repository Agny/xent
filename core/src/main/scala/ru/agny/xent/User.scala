package ru.agny.xent

import ru.agny.xent.utils.IdGen

case class User(name: String, storage: Storage) {
  val localIdGen = IdGen()

  def tick(rates: Facility => Int, craft: Facility => Resource): User = {
    User(name, storage.produce(rates, craft))
  }
}
