package ru.agny.xent

import ru.agny.xent.UserType.UserId

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

case class ActionResult(id: UserId) {
  def send(res: Future[(User, Response)]): User = {
    val u = Await.result(res, 1 second)
    println(u)
    u._1
  }
}
