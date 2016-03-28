package ru.agny.xent

case class Storage(resources: List[ResourceUnit], producers: List[Facility]) {

  def produce(rates: Facility => Int, craft: Facility => Resource): Storage = {
    val outposts = producers.collect { case x: Outpost => x }
    val buildings = producers.collect { case x: Building => x }

    val outpostsProduction = outposts.foldRight(this)((x, y) => {
      x.produce(rates(x))(y)(x.resource.name) match {
        case Left(v) => println(v); this;
        case Right(v) => v
      }
    })
    val buildingsProduction = buildings.foldRight(outpostsProduction)((x, y) => {
      x.produce(rates(x))(y)(craft(x).name) match {
        case Left(v) => println(v); y;
        case Right(v) => v
      }
    })
    buildingsProduction
  }

  def add(r: ResourceUnit): Storage = {
    resources.find(x => x.res == r.res) match {
      case Some(v) =>
        Storage(resources.map {
          case x if x.res == r.res => ResourceUnit(x.value + r.value, r.res)
          case x => x
        }, producers)
      case None => Storage(r :: resources, producers)
    }
  }

  def spend(recipe: Recipe): Either[Error, Storage] =
    recipe.cost.find(x => !resources.exists(y => x.res == y.res && y.value >= x.value)) match {
      case Some(v) => Left(Error(s"There isn't enough of ${v.res}"))
      case None =>
        Right(Storage(recipe.cost.foldRight(resources)((a, b) => b.map(bb => bb.res match {
          case a.res => ResourceUnit(bb.value - a.value, a.res)
          case _ => bb
        })),producers))
    }
}
