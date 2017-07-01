package ru.agny.xent

import ru.agny.xent.UserType.{ObjectId, UserId}
import ru.agny.xent.battle.unit.Troop
import ru.agny.xent.battle.unit.inventory.Backpack
import ru.agny.xent.core.Item.ItemId
import ru.agny.xent.core._
import ru.agny.xent.core.utils.NESeq

case class User(id: UserId, name: String, city: City, lands: Lands, queue: ProductionQueue, souls: Workers, power: LifePower, lastAction: Long) {

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
    for {
      building <- findProducer(facility).right
      storageWithB <- building.addToQueue(res)(city.storage).right
    } yield copy(city = city.update(storageWithB._2, storageWithB._1))

  def build(cell: ContainerCell, cost: Cost): Either[Response, User] =
    for {
      userFacility <- (cell match {
        case LocalCell(x, y, mb) => buildInCity(mb.get, Coordinate(x, y), cost)
        case WorldCell(x, y, mo, _, _, _) => buildOutpost(mo.get, cost)
      }).right
    } yield {
      userFacility._1.copy(queue = userFacility._1.queue.in(userFacility._2, 1))
    }

  private def findProducer(facility: ItemId) = city.producers.find(f => f.id == facility).
    map(Right(_)) getOrElse Left(Response(s"Unable to find working building $facility"))

  private def buildInCity(b: Building, where: Coordinate, cost: Cost) =
    for {
      c <- city.place(b, ShapeProvider.get(b.name).form(where)).right
      user <- copy(city = c).spend(cost).right
    } yield (user, b)

  private def buildOutpost(o: Outpost, cost: Cost) = {
    for {user <- spend(cost).right} yield (user, o)
  }

  private def spend(recipe: Cost): Either[Response, User] =
    for {
      u <- work(DoNothing).right
      s <- u.city.storage.spend(recipe).right
    } yield u.copy(city = u.city.copy(storage = s))

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
    User(id, name, city, Lands.empty, ProductionQueue.empty, Workers.empty, LifePower.default, System.currentTimeMillis())
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
