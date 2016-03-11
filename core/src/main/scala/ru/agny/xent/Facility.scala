package ru.agny.xent

sealed trait Facility {
  val name: String

  def produce(amount: Int)(recipe:String): Either[Error, List[(Resource, ResourceUnit)]]
}
case class Outpost(name: String, resource: Extractable, since: Set[Prereq]) extends Facility {
  override def produce(amount: Int)(recipe:String): Either[Error, List[(Resource, ResourceUnit)]] = {
    resource.out(amount)
  }
}
case class Building(name: String, recipes: List[Recipe], since: Set[Prereq]) extends Facility {

  override def produce(amount: Int)(recipe:String): Either[Error, List[(Resource, ResourceUnit)]] = {
    recipes.find(x => x.title == recipe).map(x => x.produce()).getOrElse(Left(Error(s"$recipe not found")))
  }
}