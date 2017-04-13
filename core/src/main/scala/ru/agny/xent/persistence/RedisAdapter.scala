package ru.agny.xent.persistence

import akka.actor.ActorSystem

import scredis._

import scala.concurrent.Future

object RedisAdapter {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val system = ActorSystem()
  val redis = Client("localhost", 9999)

  def set(e: RedisMessage): Future[Boolean] = redis.hSet(e.collectionId, e.key, e.toPersist)

  def get(collectionId: String): Future[Vector[RedisMessage]] =
    redis.hGetAll(collectionId).map {
      case Some(v) => v.flatMap(kv => MessageHandler.convert(kv._2))(collection.breakOut)
      case None => Vector.empty[RedisMessage]
    }

  def keys() = redis.keys("user*")

  def info() = redis.info("memory")

}
