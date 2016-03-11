package ru.agny.xent

sealed trait Resource {
  val name: String
  val since: Set[Prereq]

  def out(amount: Int): Either[Error, List[(Resource, ResourceUnit)]]
}
case class Extractable(name: String, volume: Int, since: Set[Prereq]) extends Resource {
  override def out(amount: Int): Either[Error, List[(Resource, ResourceUnit)]] = {
    if (amount > volume) Left(Error(s"Not enough $name"))
    else Right(List((Extractable(name, volume - amount, since), ResourceUnit(this, amount))))
  }
}
case class Producible(name: String, recipe: Recipe, since: Set[Prereq]) extends Resource {
  override def out(amount: Int): Either[Error, List[(Resource, ResourceUnit)]] = {
    recipe.produce()
  }
}
case class ResourceUnit(res: Resource, amount: Int)
case class Cost(spend: Int)
object Cost {
  implicit def costToInt(x: Cost): Int = x.spend

  implicit def intToCost(x: Int): Cost = Cost(x)
}