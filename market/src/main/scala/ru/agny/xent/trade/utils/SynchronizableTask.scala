package ru.agny.xent.trade.utils

import java.util.concurrent.Callable

class SynchronizableTask[T](val syncField: Any, body: => T) extends Callable[T] {
  override def call() = body
}
