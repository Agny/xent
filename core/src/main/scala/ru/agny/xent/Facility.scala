package ru.agny.xent

sealed trait Facility[T <: Placed] {
  val name: String
}
case class Outpost(name: String) extends Facility[Global]
case class Building(name: String) extends Facility[Local]

object Facility {
  def apply(): Facility = {
    ???
  }
}