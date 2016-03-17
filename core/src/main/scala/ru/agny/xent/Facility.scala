package ru.agny.xent

sealed trait Facility {
  val id: Int
  val name: String
  type ResourceName = String

  def produce(amount: Int): Storage => ResourceName => Either[Error, Storage]
}
case class Outpost(id: Int, name: String, resource: Extractable, since: Set[Prereq]) extends Facility {
  override def produce(amount: Int): Storage => ResourceName => Either[Error, Storage] = storage =>
    resourceName =>
      if (resource.name == resourceName) Right(storage.add(resource.out(amount)))
      else Left(Error(s"This facility can't extract $resourceName"))
}
case class Building(id: Int, name: String, resources: List[Producible], since: Set[Prereq]) extends Facility {

  override def produce(amount: Int): Storage => ResourceName => Either[Error, Storage] = storage =>
    resourceName => {
      resources.find(x => x.name == resourceName) match {
        case Some(v) =>
          val bulkCost = v.recipe.cost.map(y => ResourceUnit(y.value * amount, y.res))
          storage.spend(Recipe(bulkCost)) match {
            case Left(s) => Left(s)
            case Right(s) =>
              val sdf = v.out(amount)
              val df = s.add(sdf)
              Right(df)
          }
        case None => Left(Error(s"This facility can't produce $resourceName"))
      }
    }
}