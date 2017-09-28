package ru.agny.xent.core

import ru.agny.xent.action.{DoNothing, UserAction}
import ru.agny.xent.battle.MovementPlan
import ru.agny.xent.battle.unit.{Backpack, Troop}
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.core.utils.UserType.{ObjectId, UserId}
import ru.agny.xent.core.city._
import ru.agny.xent.core.inventory.{Cost, ItemStack, ProductionQueue}
import ru.agny.xent.core.utils.NESeq
import ru.agny.xent.messages.Response

case class User(id: UserId, name: String, city: City, queue: ProductionQueue, souls: Workers, power: LifePower, lastAction: Long) {

  import User._

  def work(a: UserAction): Either[Response, User] = {
    val time = System.currentTimeMillis()
    val period = time - lastAction
    val (q, prod) = queue.out(period)
    val updatedCity = city.produce(period)
    val actualUser = copy(city = updatedCity, queue = q, lastAction = time)
    val updated = handleQueue(prod.map { case (item, amount) => item.asInstanceOf[Building] }, actualUser)
    a.run(updated)
  }

  def addProduction(facility: ItemId, res: ItemStack): Either[Response, User] =
    for {
      building <- findProducer(facility)
      storageWithB <- building.addToQueue(res)(city.storage)
    } yield copy(city = city.update(storageWithB._2, storageWithB._1))

  def build(cell: Cell, cost: Cost): Either[Response, User] =
    for {
      userFacility <- cell match {
        case b: Building => buildInCity(b, cost)
        case o: Outpost => buildOutpost(o, cost)
      }
    } yield {
      userFacility._1.copy(queue = userFacility._1.queue.in(userFacility._2, 1))
    }

  def createTroop(troopId: ObjectId, soulsId: Vector[ObjectId]): Either[Response, (User, Troop)] = {
    val (remains, units) = souls.callToArms(soulsId)
    if (units.nonEmpty) Right((copy(souls = remains), Troop(troopId, NESeq(units), Backpack.empty, id, MovementPlan.idle(city.c))))
    else Left(Response(s"Cannot create troop with souls $soulsId"))
  }

  private def findProducer(facility: ItemId) = city.producers.find(f => f.id == facility).
    map(Right(_)) getOrElse Left(Response(s"Unable to find working building $facility"))

  private def buildInCity(b: Building, cost: Cost) =
    for {
      c <- city.place(b, ShapeProvider.get(b.name).form(b.c))
      user <- copy(city = c).spend(cost)
    } yield (user, b)

  private def buildOutpost(o: Outpost, cost: Cost) = {
    for {user <- spend(cost)} yield (user, o)
  }

  private def spend(recipe: Cost): Either[Response, User] =
    for {
      u <- work(DoNothing)
      s <- u.city.storage.spend(recipe)
    } yield u.copy(city = u.city.copy(storage = s))

  override def toString = s"id=$id name=$name time=$lastAction"
}

object User {
  def apply(id: UserId, name: String, city: City): User = {
    User(id, name, city, ProductionQueue.empty, Workers.empty, LifePower.default, System.currentTimeMillis())
  }

  private def handleQueue(items: Vector[Building], state: User): User = items match {
    case h +: t =>
      val update = state.copy(city = state.city.update(h.finish))
      handleQueue(t, update)
    case _ => state
  }
}


