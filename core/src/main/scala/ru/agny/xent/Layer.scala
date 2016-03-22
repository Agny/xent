package ru.agny.xent

import scala.util.Random

case class Layer(id: String, resources: List[Extractable]) {

  val size = 30
  val cells = generateMap()

  def generateMap(): List[WorldCell] = {
    def genByX(x: Int)(y: Int, acc: List[WorldCell]): List[WorldCell] = {
      if (x < size) genByX(x + 1)(y, WorldCell(x, y, mbResource()) :: acc)
      else WorldCell(x, y, mbResource()) :: acc
    }

    (0 to size).flatMap(y => genByX(0)(y, List.empty)).toList
  }

  def mbResource(): List[Extractable] = {
    val threshold = 94
    Random.nextInt(100) match {
      case c if c > threshold => List(generateResource())
      case _ => List.empty
    }
  }

  def generateResource(): Extractable = ???

  def tick(users: List[User], actions: Facility => Resource): List[Resource] = {
    val updatedUsers = users.map(x => x.tick(rates, actions))
    val updatedResources = updatedUsers.flatMap(x => x.storage.resources)
    println(updatedResources)
    Thread.sleep(5000)
    tick(updatedUsers, actions)
  }

  def rates(f: Facility): Int = {
    f match {
      case x => 1
    }
  }

}
