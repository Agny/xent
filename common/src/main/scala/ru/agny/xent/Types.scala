package ru.agny.xent

import java.util.UUID

type Distance = Int
type ItemId = Long
type ItemWeight = Int
type PlayerId = Long
object PlayerId {
  val Neutral: PlayerId = 0
  val AIEnemy: PlayerId = -1
  val Lost: PlayerId = -2
}
type RealmId = UUID
type TimeInterval = Int //seconds
object TimeInterval {
  val Zero: TimeInterval = 0
  val BaseRound: TimeInterval = 300
  extension (v: Double)
    def toInterval: TimeInterval = v.toInt
}
type Velocity = Int
object Velocity {
  val Max: Velocity = 10000
}