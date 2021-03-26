package ru.agny.xent

import java.util.UUID

type Distance = Int
type ItemId = Long
type ItemWeight = Int
type PlayerId = Long
type RealmId = UUID
type TimeInterval = Int //seconds
type Velocity = Int
object Velocity {
  val Max: Velocity = 10000
}