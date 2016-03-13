package ru.agny.xent

sealed trait Facility {
  val name: String
  val id: Int

  def produce(amount: Int): String => Either[Error, (Int, Resource)]
}
case class Outpost(name: String, id: Int, resource: Extractable, since: Set[Prereq]) extends Facility {
  override def produce(amount: Int): String => Either[Error, (Int, Resource)] =
    resourceName =>
      if (resource.name == resourceName) Right(resource.out(amount))
      else Left(Error(s"This facility can't extract $resourceName"))
}
case class Building(name: String, id: Int, resources: List[Producible], since: Set[Prereq]) extends Facility {

  override def produce(amount: Int): String => Either[Error, (Int, Resource)] =
    resourceName => {
      resources.find(x => x.name == resourceName) match {
        case Some(v) => Right(v.out(amount))
        case None => Left(Error(s"This facility can't produce $resourceName"))
      }
    }
}