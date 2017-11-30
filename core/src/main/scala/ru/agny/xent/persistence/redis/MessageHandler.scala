package ru.agny.xent.persistence.redis

import scala.collection.mutable

/**
  * Registry keeps all conversion functions from serialized redis string to a <code>Loggable</code> instance.
  * Functions are taken from each class annotated with the <code>RedisEntity</code> when application starts.
  * It's done this way, although there is an alternative with scanning classpath for all annotated classes, getting from them functions etc.
  *
  * @see [[RedisEntity]]
  */
object MessageHandler {
  private val registry = mutable.Map[String, String => Loggable]()
  private val classNameR = """^(\w+)\(.*""".r

  def register(className: String, f: String => Loggable) = registry += (className -> f)

  def convert(v: String): Option[Loggable] =
    v match {
      case classNameR(key) => Some(registry(key)(v))
      case _ => None
    }
}