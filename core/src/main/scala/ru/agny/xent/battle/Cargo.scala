package ru.agny.xent.battle

import ru.agny.xent.UserType.{ObjectId, UserId}
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.unit.equip.OutcomeDamage
import ru.agny.xent.core.utils.NESeq
import ru.agny.xent.core.{ItemStack, MapObject}

case class Cargo(id: ObjectId,
                 user: UserId,
                 body: NESeq[Guard],
                 resources: ItemStack,
                 private val plan: MovementPlan) extends MapObject {
  override type Self = Cargo

  override val weight = body.map(_.weight).sum

  override def pos(time: ProgressTime) = if (isActive) plan.now(Guard.speed, time) else plan.now(0, time)

  override val isActive = body.exists(_.spirit > 0)

  override val isAbleToFight = false

  override def isDiscardable = resources.stackValue == 0 || plan.now(Guard.speed, 0) == plan.home

  override val isAggressive = false

  override def concede() = (copy(resources = ItemStack(0, resources.id)), Vector(resources))

  override def receiveDamage(d: OutcomeDamage, targeted: Vector[ObjectId]) = {
    val souls = body.filter(_.spirit > 0).map {
      case u if targeted.contains(u.id) => u.receiveDamage(d)
      case unharmed => unharmed
    }
    copy(body = NESeq(souls.head, souls.tail))
  }
}
