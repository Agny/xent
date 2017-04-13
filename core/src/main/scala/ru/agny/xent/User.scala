package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core._

case class User(id: UserId, name: String, city: City, lands: Lands, storage: Storage, queue: ProductionQueue, lastAction: Long) {

  private lazy val producers = city.producers ++ lands.outposts

  def work(a: UserAction): Either[Response, User] = {
    val time = System.currentTimeMillis()
    val (q, prod) = queue.out(lastAction)
    val (actualStorage, updatedFacilities) = storage.tick(lastAction, producers)
    val (updatedCity, updatedLands) = updateByFacilitiesQueue(updatedFacilities)
    val actualUser = copy(city = updatedCity, lands = updatedLands, storage = actualStorage, queue = q, lastAction = time)
    val updated = handleQueue(prod.map { case (item, amount) => item.asInstanceOf[Facility] }, actualUser)
    a.run(updated)
  }

  def addProduction(facility: Facility, res: ResourceUnit) = {
    facility.addToQueue(res)(storage)
  }

  def build(cell: ContainerCell): User = {
    val facility = cell.building.get match {
      case x: Building => x.copy(state = Facility.InConstruction)
      case x: Outpost => x.copy(state = Facility.InConstruction)
    }
    val q = queue.in(facility, 1)
    //TODO modify citymap
    copy(queue = q)
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

  def findFacility(producer: String): Option[Facility] = {
    producers.find(_.name == producer)
  }

  private def handleQueue(items: Vector[Facility], state: User): User = items match {
    case h +: t =>
      val update = h match {
        case x: Building => state.copy(city = city.build(x))
        case x: Outpost => state.copy(lands = lands.add(x))
      }
      handleQueue(t, update)
    case _ => state
  }

  private def updateByFacilitiesQueue(f: Vector[Facility]): (City, Lands) = {
    val (buildings, outposts) = f.foldLeft((Vector.empty[Building], Vector.empty[Outpost]))((s, x) => {
      x match {
        case a: Building => (s._1 :+ a, s._2)
        case a: Outpost => (s._1, s._2 :+ a)
      }
    })
    (city.update(buildings.map((_, Facility.InProcess))), Lands(outposts))
  }

  override def toString = s"id=$id name=$name time=$lastAction"
}

case class Lands(outposts: Vector[Outpost]) {
  def add(outpost: Outpost): Lands = Lands(outpost +: outposts)
}

object Lands {
  def empty = Lands(Vector.empty)
}

object User {
  def apply(id: UserId, name: String, city: City): User = {
    User(id, name, city, Lands.empty, Storage.empty, ProductionQueue.empty, System.currentTimeMillis())
  }
}

object UserType {
  type UserId = Long
  type ObjectId = Long
}
