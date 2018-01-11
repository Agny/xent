package ru.agny.xent.trade.utils

import java.util.concurrent.{Callable, Executors}

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

/**
  * Pool for executing partially synchronized task
  */
class SynchronizedPool() {
  val threadPool = Executors.newCachedThreadPool()
  val locks = TrieMap.empty[Any, AnyRef] //TODO NB:schedule cleanup / replace by simple lru cache ?

  def submit[T](v: Callable[T]): Future[T] = {
    v match {
      case t: SynchronizableTask[T] =>
        locks.putIfAbsent(t.syncField, new Object())
        val task = new WrapperTask[T](t, locks.get(t.syncField))
        Future.successful(threadPool.submit(task).get())
    }
  }
}
