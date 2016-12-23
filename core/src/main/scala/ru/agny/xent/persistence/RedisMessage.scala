package ru.agny.xent.persistence

/**
  * Trait for working with Redis persistence.
  * Can be implemented manually or through annotating target class with [[RedisEntity]]
  */

trait RedisMessage {
  def collectionId: String
  def key: String

  def toPersist: String
}
