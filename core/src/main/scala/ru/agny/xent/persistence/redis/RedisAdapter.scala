package ru.agny.xent.persistence.redis

import akka.actor.ActorSystem
import com.redis.RedisClient

import scala.concurrent.Future

object RedisAdapter {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val system = ActorSystem()
  val redis = new RedisClient("localhost", 9999)

  def set(e: Loggable): Future[Boolean] = Future(redis.hset(e.collectionId, e.key, e.toPersist))

  def get(collectionId: String): Future[Vector[Loggable]] = Future(
    redis.hgetall1(collectionId) match {
      case Some(v) => v.flatMap(kv => MessageHandler.convert(kv._2))(collection.breakOut)
      case None => Vector.empty[Loggable]
    })

  def keys() = redis.keys("user*")

  def info() = redis.info("memory")

}
