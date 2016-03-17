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
        case Left(v) => println(v); this;
        case Right(v) => v
      }
    })
    buildingsProduction
  }

  def add(r: ResourceUnit): Storage = {
    resources.find(x => x.res.name == r.res.name) match {
      case Some(v) =>
        Storage(resources.map {
          case x if x.res.name == r.res.name => ResourceUnit(x.value + r.value, r.res)
          case x => x
        }, producers)
      case None => Storage(r :: resources, producers)
    }
  }

  def spend(recipe: Recipe): Either[Error, Storage] = {
    val diff = recipe.cost.map(x => resources.find(y => y.res.name == x.res.name) match {
      case Some(v) => ResourceUnit(v.value - x.value, v.res)
      case None => ResourceUnit(0 - x.value, x.res)
    })
    val balance = resources.map(x => recipe.cost.find(y => y.res.name == x.res.name) match {
      case Some(v) => ResourceUnit(x.value - v.value, x.res)
      case None => x
    })
    diff.find(x => x.value < 0) match {
      case Some(v) => Left(Error(s"There isn't enough of ${v.res}. Needed ${0 - v.value} more"))
      case None => Right(Storage(balance, producers))
    }
  }
}
