package ru.agny.xent.core

import ru.agny.xent.UserType.{ObjectId, UserId}
import ru.agny.xent.core.Progress.ProgressTime
import ru.agny.xent.core.unit.equip.OutcomeDamage

//TODO Think about more appropriate name, which describes object on the world map: troops/cargos/outposts/cities etc.
abstract class MapObject {
  val id: ObjectId
  val user: UserId
  val weight: Int

  def pos(time: ProgressTime): Coordinate

  /** @return true if this object is able to participate in activities */
  def isActive: Boolean

  /** @return true if this object is able to pa */
  def isAbleToFight: Boolean

  /** @return true if this object can be discarded from global map and be assimilated by user */
  def isDiscardable: Boolean

  /** @return true if this object makes choice to participate in the ongoing battle */
  def isAggressive: Boolean

  /** return fallen state of this object along with loot list */
  def concede(): (MapObject, Vector[Item])

  def receiveDamage(d: OutcomeDamage, targeted: Vector[ObjectId]): MapObject
}
