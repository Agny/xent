package ru.agny.xent.bench

import ru.agny.xent.persistence.{RedisEntity, RedisMessage}

@RedisEntity("test", "id", "s")
case class Foo(id: Long, values: Vector[Int], s: String, inner: Bar) extends RedisMessage
