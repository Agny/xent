package ru.agny.xent.trade.utils

import java.util
import java.util.Collections
import java.util.concurrent.{Callable, Executors}

import scala.concurrent.Future

/**
  * Pool for executing partially synchronized task
  */
class SynchronizedPool() {
  val threadPool = Executors.newCachedThreadPool()
  val locks = Collections.synchronizedMap(new util.WeakHashMap[Any, AnyRef]())

  def submit[T](v: Callable[T]): Future[T] = {
    v match {
      case t: SynchronizableTask[T] =>
        locks.putIfAbsent(t.syncField, new Object())
        Future.successful(threadPool.submit(new WrapperTask[T](t, locks.get(t.syncField))).get())
    }
  }
}
