package ru.agny.xent.messages

import java.util.concurrent.locks.ReentrantLock

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class MessageQueue[T]() {

  private var messages: Vector[T] = Vector.empty
  private val lock = new ReentrantLock()

  def push[M <: T](msg: M): Future[M] = {
    pushUntilSuccess(msg)
  }

  private def pushUntilSuccess[M <: T](msg: M): Future[M] = Future {
    println(s"MESSAGE: $msg")
    lock.lock()
    messages = msg +: messages
    lock.unlock()
    msg
  }

  def take(): Vector[T] = {
    lock.lock()
    val res = messages
    messages = Vector.empty
    lock.unlock()
    res.reverse
  }
}

object MessageQueue {
  val global = MessageQueue[Responder[_]]() //TODO queue for each type?
}
