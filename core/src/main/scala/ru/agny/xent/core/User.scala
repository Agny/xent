package ru.agny.xent.core

import ru.agny.xent.action.{DoNothing, UserAction}
import ru.agny.xent.battle.{MovementPlan, Outpost}
import ru.agny.xent.battle.unit.{Backpack, Troop}
import ru.agny.xent.core.inventory.Item.{ItemId, ItemWeight}
import ru.agny.xent.core.utils.UserType.{ObjectId, UserId}
import ru.agny.xent.core.city._
import ru.agny.xent.core.inventory.{Cost, Item, ItemStack, ProductionQueue}
import ru.agny.xent.core.utils.{ErrorCode, NESeq}

case class User(id: UserId, name: String, city: City, queue: ProductionQueue, souls: Workers, power: LifePower, lastAction: Long) {

  import User._

  def work(a: UserAction): User = {
    val time = System.currentTimeMillis()
    val period = time - lastAction
    val (q, prod) = queue.out(period)
    val updatedCity = city.produce(period)
    val actualUser = copy(city = updatedCity, queue = q, lastAction = time)
    val updated = handleQueue(prod.map { case (item, amount) => item.asInstanceOf[Building] }, actualUser)
    a.run(updated)
  }

  def addProduction(facility: ItemId, res: ItemStack): Either[ErrorCode.Value, User] =
    for {
      building <- findProducer(facility)
      storageWithB <- building.addToQueue(res)(city.storage)
    } yield copy(city = city.update(storageWithB._2, storageWithB._1))

  def build(cell: Cell, cost: Cost): Either[ErrorCode.Value, User] =
    for {
      userFacility <- cell match {
        case b: Building => buildInCity(b, cost)
        case o: Outpost => buildOutpost(o, cost)
      }
    } yield {
      userFacility._1.copy(queue = userFacility._1.queue.in(userFacility._2, 1))
    }

  def createTroop(troopId: ObjectId, soulsId: Vector[ObjectId]): Either[ErrorCode.Value, (User, Troop)] = {
    val (remains, units) = souls.callToArms(soulsId)
    if (units.nonEmpty) Right((copy(souls = remains), Troop(troopId, NESeq(units), Backpack.empty, id, MovementPlan.idle(city.c))))
    else Left(ErrorCode.TROOP_CANT_BE_CREATED)
  }

  def assimilateTroop(v: Troop): User = {
    val (lifePower, items) = v.disband()
    copy(power = power.regain(lifePower, lifePower / 10), city = city.addResources(items))
  }

  private def findProducer(facility: ItemId) = city.producers.find(f => f.id == facility).
    map(Right(_)) getOrElse Left(ErrorCode.NOT_ENOUGH_RESOURCES)

  private def buildInCity(b: Building, cost: Cost) =
    for {
      c <- city.place(b, ShapeProvider.get(b.name).form(b.c))
      user <- copy(city = c).spend(cost)
    } yield (user, b)

  private def buildOutpost(o: Outpost, cost: Cost) = {
    for {user <- spend(cost)} yield (user, o)
  }

  def spend(recipe: Cost): Either[ErrorCode.Value, User] = {
    val u = work(DoNothing)
    for {s <- u.city.storage.spend(recipe)} yield u.copy(city = u.city.copy(storage = s))
  }

  def loseResources(weight: ItemWeight): (User, Vector[Item]) = {
    val u = work(DoNothing)
    val toLose = u.city.storage.loadInWeight(weight)
    spend(Cost(toLose)) match {
      case Left(_) => (u.copy(city = u.city.copy(storage = Storage.empty)), u.city.storage.items)
      case Right(v) => (v, toLose)
    }
  }

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


