package ru.agny.xent

import ru.agny.xent.UserType.{ObjectId, UserId}
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.battle.unit.inventory.Backpack
import ru.agny.xent.core._
import ru.agny.xent.core.utils.{SubTyper, NESeq}

case class User(id: UserId, name: String, city: City, lands: Lands, storage: Storage, queue: ProductionQueue, souls: Workers, lastAction: Long) {

  import User._
  import FacilitySubTyper.implicits._

  private lazy val producers = city.producers ++ lands.outposts

  def work(a: UserAction): Either[Response, User] = {
    val time = System.currentTimeMillis()
    val (q, prod) = queue.out(lastAction)
    val (actualStorage, updatedFacilities) = storage.tick(lastAction, producers)
    val (buildings, outposts) = SubTyper.partition[Building, Outpost, Facility](updatedFacilities)
    val updatedCity = city.update(buildings.map((_, Facility.Working)))

    val actualUser = copy(city = updatedCity, lands = Lands(outposts), storage = actualStorage, queue = q, lastAction = time)
    val updated = handleQueue(prod.map { case (item, amount) => item.asInstanceOf[Facility] }, actualUser)
    a.run(updated)
  }

  def addProduction(facility: Facility, res: ItemStack) = {
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

  def createTroop(troopId: ObjectId, soulsId: Vector[ObjectId]): (User, Option[Troop]) = {
    val (remains, units) = souls.callToArms(soulsId)
    if (units.nonEmpty) (copy(souls = remains), Some(Troop(troopId, NESeq(units), Backpack.empty, id, city.c)))
    else (this, None)
  }

  override def toString = s"id=$id name=$name time=$lastAction"
}

case class Lands(outposts: Vector[Outpost]) {
  def add(outpost: Outpost): Lands = Lands(outpost +: outposts)
}

object Lands {
  val empty = Lands(Vector.empty)
}

object User {
  def apply(id: UserId, name: String, city: City): User = {
    User(id, name, city, Lands.empty, Storage.empty, ProductionQueue.empty, Workers.empty, System.currentTimeMillis())
  }

  private def handleQueue(items: Vector[Facility], state: User): User = items match {
    case h +: t =>
      val update = h match {
        case x: Building => state.copy(city = state.city.build(x))
        case x: Outpost => state.copy(lands = state.lands.add(x))
      }
      handleQueue(t, update)
    case _ => state
  }
}

object UserType {
  type UserId = Long
  type ObjectId = Long
}
