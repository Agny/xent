package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core._

object UserType {
  type UserId = Long
}

case class User(id: UserId, name: String, city: City, private val storage: Storage, lastAction: Long) {

  def work(a: UserAction): Either[Response, User] = {
    a.run(User(id, name, city, storage.tick(lastAction)))
  }

  def addToQueue(facility: Facility, res: ResourceUnit) = {
    facility.addToQueue(res)(storage)
  }

  def spend(recipe: Cost): Either[Response, User] = {
    work(DoNothing) match {
      case Left(v) => Left(v)
      case Right(v) =>
        v.storage.spend(recipe) match {
          case Left(s) => Left(s)
          case Right(s) => Right(v.copy(storage = s))
        }
    }
  }

  def addFacility(producer: Facility): User = {
    work(DoNothing) match {
      case Left(v) => this
      case Right(v) => v.copy(storage = v.storage.add(producer))
    }
  }

  def addBuilding(buildingCell: LocalCell): User = {
    addFacility(buildingCell.building.get).copy(city = city.update(buildingCell))
  }

  def findFacility(producer: String): Option[Facility] = {
    storage.producers.find(_.name == producer)
  }

  override def toString = s"id=$id name=$name time=$lastAction"
}

object User {
  def apply(id: UserId, name: String, city: City): User = {
    val defaultBuildings = city.flatMap(c => c.building)
    User(id, name, city, Storage.empty.copy(producers = defaultBuildings), System.currentTimeMillis())
  }

  def apply(id: UserId, name: String, city: City, storage: Storage): User = User(id, name, city, storage, System.currentTimeMillis())
}
