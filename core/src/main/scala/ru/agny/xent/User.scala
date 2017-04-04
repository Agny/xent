package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core._

case class User(id: UserId, name: String, city: City, lands: Lands, storage: Storage, queue: ProductionQueue, lastAction: Long) {

  def work(a: UserAction): Either[Response, User] = {
    val time = System.currentTimeMillis()
    val (q, prod) = queue.out(lastAction)
    val actualStorage = storage.tick(lastAction)
    val actualUser = copy(storage = actualStorage, queue = q, lastAction = time)
    val updated = handleQueue(prod.map(_._1.asInstanceOf[Facility]), actualUser)
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

  private def handleQueue(items: Seq[Facility], state: User): User = {
    items match {
      case Seq(h, t@_*) =>
        val update = h match {
          case x: Building => state.copy(city = city.build(x), storage = storage.addProducer(x))
          case x: Outpost => state.copy(lands = lands.add(x), storage = storage.addProducer(x))
        }
        handleQueue(t, update)
      case _ => state
    }
  }

  def findFacility(producer: String): Option[Facility] = {
    storage.producers.find(_.name == producer)
  }

  override def toString = s"id=$id name=$name time=$lastAction"
}

case class Lands(outposts: Seq[Outpost]) {
  def add(outpost: Outpost): Lands = Lands(outposts :+ outpost)
}

object Lands {
  def empty() = Lands(Seq.empty)
}

object User {
  def apply(id: UserId, name: String, city: City): User = {
    val storageEmpty = Storage.empty.copy(producers = city.buildings().map(_.building.get))
    User(id, name, city, Lands.empty(), storageEmpty, ProductionQueue.empty(), System.currentTimeMillis())
  }
}

object UserType {
  type UserId = Long
}
