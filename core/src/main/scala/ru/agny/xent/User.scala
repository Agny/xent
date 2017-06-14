package ru.agny.xent

import ru.agny.xent.UserType.{ObjectId, UserId}
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.battle.unit.inventory.Backpack
import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core._
import ru.agny.xent.core.utils.{SubTyper, NESeq}

case class User(id: UserId, name: String, city: City, lands: Lands, queue: ProductionQueue, souls: Workers, lastAction: Long) {

  import User._

  def work(a: UserAction): Either[Response, User] = {
    val time = System.currentTimeMillis()
    val period = time - lastAction
    val (q, prod) = queue.out(period)
    val (updatedCity, updatedOutposts) = city.produce(period, lands.outposts)
    val actualUser = copy(city = updatedCity, lands = Lands(updatedOutposts), queue = q, lastAction = time)
    val updated = handleQueue(prod.map { case (item, amount) => item.asInstanceOf[Facility] }, actualUser)
    a.run(updated)
  }

  def addProduction(facility: ItemId, res: ItemStack): Either[Response, User] =
    city.producers.find(_.id == facility) match {
      case Some(v) => v.addToQueue(res)(city.storage) match {
        case Left(l) => Left(l)
        case Right((s, f)) => Right(copy(city = city.update(f, s)))
      }
      case _ => Left(Response(s"Unable to find building $facility"))
    }

  def build(cell: ContainerCell): User = {
    val facility = cell.building.get.build
    val q = queue.in(facility, 1)
    //TODO modify citymap
    copy(queue = q)
  }

  def spend(recipe: Cost): Either[Response, User] = {
    work(DoNothing) match {
      case Left(v) => Left(v)
      case Right(v) =>
        v.city.storage.spend(recipe) match {
          case Left(s) => Left(s)
          case Right(s) => Right(v.copy(city = v.city.copy(storage = s)))
        }
    }
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
    User(id, name, city, Lands.empty, ProductionQueue.empty, Workers.empty, System.currentTimeMillis())
  }

  private def handleQueue(items: Vector[Facility], state: User): User = items match {
    case h +: t =>
      val update = h.finish match {
        case x: Building => state.copy(city = state.city.update(x))
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
