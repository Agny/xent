package ru.agny.xent

import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

//Class is not thread safe: it shouldn't be used in scenarios where is several consumer threads exist
case class MessageQueue() {

  var messages: Seq[(Message, Long)] = Seq.empty
  val lock = new ReentrantReadWriteLock()

  def push(msg: Message, number: Long): Future[Long] = {
    pushUntilSuccess(msg, number)
  }

  private def pushUntilSuccess(msg: Message, number: Long): Future[Long] = Future {
    println(s"MESSAGE: $msg")
    val rLock = lock.readLock()
    while (!rLock.tryLock()) Thread.sleep(10)
    messages = (msg, number) +: messages
    rLock.unlock()
    number
  }

  def take(): Seq[Message] = {
    val res = messages.map(_._1).reverse
    lock.writeLock().tryLock()
    messages = Seq.empty
    lock.writeLock().unlock()
    res
  }
}
