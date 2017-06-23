package ru.agny.xent

import java.util.concurrent.locks.ReentrantLock

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class MessageQueue[T]() {

  private var messages: Vector[(T, Long)] = Vector.empty
  private val lock = new ReentrantLock()

  def push(msg: T, number: Long): Future[Long] = {
    pushUntilSuccess(msg, number)
  }

  private def pushUntilSuccess(msg: T, number: Long): Future[Long] = Future {
    println(s"MESSAGE: $msg")
    lock.lock()
    messages = (msg, number) +: messages
    lock.unlock()
    number
  }

  def take(): Vector[T] = {
    lock.lock()
    val res = messages
    messages = Vector.empty
    lock.unlock()
    res.map(_._1).reverse
  }
}
