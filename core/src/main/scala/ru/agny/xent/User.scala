package ru.agny.xent

import ru.agny.xent.UserType.UserId
import ru.agny.xent.core._

case class User(id: UserId, name: String, city: City, facilities: Map[(String, Facility.State), ContainerCell], storage: Storage, queue: ProductionQueue, lastAction: Long) {

  def work(a: UserAction): Either[Response, User] = {
    val time = System.currentTimeMillis()
    val (q, prod) = queue.out(lastAction)
    val actualStorage = storage.tick(lastAction)
    val actualUser = copy(storage = actualStorage, queue = q, lastAction = time)
    val updated = handleQueue(prod.map { case (item, amount) => item.asInstanceOf[Facility] }, actualUser)
    a.run(updated)
  }

  def addProduction(facility: Facility, res: ResourceUnit) = {
    facility.addToQueue(res)(storage)
  }

  def build(cell: ContainerCell): User = {
    val facility = cell.building.get
    val b = facilities + ((facility.name, Facility.InConstruction) -> cell)
    val q = queue.in(facility, 1)
    //TODO modify citymap
    copy(facilities = b, queue = q)
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
        val update = facilities(h.name, Facility.InConstruction) match {
          case c: LocalCell =>
            val cityMap = city.build(c, c.building.get)
            val updatedBuildings = updateBuildingState(h.name, Facility.InConstruction, Facility.Idle)
            state.copy(city = cityMap, facilities = updatedBuildings, storage = storage.addProducer(h))
          case c: WorldCell =>
            val updatedBuildings = updateBuildingState(h.name, Facility.InConstruction, Facility.Idle)
            state.copy(facilities = updatedBuildings, storage = storage.addProducer(h))
        }
        handleQueue(t, update)
      case _ => state
    }
  }

  private def updateBuildingState(f: String, from: Facility.State, to: Facility.State) = {
    facilities.map { case facility@(facilityToUpdate, cell) => if (facilityToUpdate ==(f, from)) (f, to) -> cell else facility }
  }

  def findFacility(producer: String): Option[Facility] = {
    storage.producers.find(_.name == producer)
  }

  override def toString = s"id=$id name=$name time=$lastAction"
}

object User {
  def apply(id: UserId, name: String, city: City): User = {
    val idle: Facility.State = Facility.Idle
    val defaultBuildings = city.buildings().map(x => (x.building.get.name, idle) -> x).toMap
    val storageEmpty = Storage.empty.copy(producers = city.buildings().map(_.building.get))
    User(id, name, city, defaultBuildings, storageEmpty, ProductionQueue.empty(), System.currentTimeMillis())
  }
}

object UserType {
  type UserId = Long
  type ObjectId = Long
}
