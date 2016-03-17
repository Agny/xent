package ru.agny.xent

case class User(name: String, storage: Storage) {

  def tick(rates: Facility => Int, craft: Facility => Resource): User = {
    User(name, storage.produce(rates, craft))
  }
}
