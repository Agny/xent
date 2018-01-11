package ru.agny.xent.trade.utils

import java.util.concurrent.Callable

case class WrapperTask[T](sync: SynchronizableTask[T], lock: AnyRef) extends Callable[T] {
  override def call() = lock.synchronized(sync.call())
}
