package ru.agny.xent

sealed trait Resource[T <: Placed] {
  val name: String
}
case class Extractable(name: String) extends Resource[Global]
case class Producible(name: String) extends Resource[Local]

object Resource {
  def apply(): Resource = {
    ???
  }
}
