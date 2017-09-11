package ru.agny.xent.battle

import ru.agny.xent.UserType.ObjectId

trait Targetable {
  val id: ObjectId
  val spirit: Int
}
