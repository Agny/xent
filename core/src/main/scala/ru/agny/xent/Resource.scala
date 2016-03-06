package ru.agny.xent

sealed trait Resource[T <: Placed] {
  val name: String

  def produce(amount: Int): (Resource, ResourceUnit) // redo with functional state
}
case class Extractable(name: String, volume: Int) extends Resource[Global] {
  override def produce(amount: Int): (Resource, ResourceUnit) = {
    if (amount > volume) (this, ResourceUnit(this, 0))
    else (Extractable(this.name, volume - amount), ResourceUnit(this, amount))
  }
}
case class Producible(name: String) extends Resource[Local] {
  override def produce(amount: Int): (Resource, ResourceUnit) = (this, ResourceUnit(this, amount))
}

case class ResourceUnit(res: Resource, amount: Int)
