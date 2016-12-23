package ru.agny.xent.persistence

/**
  * Trait for working with Redis persistence.
  * Can be implemented manually or through annotating target class with [[RedisEntity]]
  */

trait RedisMessage {
  val collectionId: String
  val key: String

  def toPersist: String
}
