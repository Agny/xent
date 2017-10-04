package ru.agny.xent.battle.unit

import ru.agny.xent.battle.{MapObject, MovementPlan}
import ru.agny.xent.core.utils.UserType.{ObjectId, UserId}
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.unit.equip.OutcomeDamage
import ru.agny.xent.core.utils.{NESeq, SelfAware}

case class Cargo(id: ObjectId,
                 user: UserId,
                 body: NESeq[Guard],
                 resources: Vector[ItemStack],
                 private val plan: MovementPlan) extends MapObject with SelfAware {
  override type Self = Cargo

  override val weight = body.map(_.weight).sum

  override def pos(time: ProgressTime) = if (isActive) plan.now(Guard.speed, time) else plan.now(0, time)

  override val isActive = body.exists(_.spirit > 0)

  override val isAbleToFight = false

  override def isDiscardable = resources.isEmpty || plan.now(Guard.speed, 0) == plan.home

  override val isAggressive = false

  override def concede() = (copy(resources = Vector.empty), resources)

  override def receiveDamage(d: OutcomeDamage, targeted: Vector[ObjectId]) = {
    val souls = body.filter(_.spirit > 0).map {
      case u if targeted.contains(u.id) => u.receiveDamage(d)
      case unharmed => unharmed
    }
    copy(body = NESeq(souls.head, souls.tail))
  }
}
