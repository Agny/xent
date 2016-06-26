package ru.agny.xent.core

import ru.agny.xent.Response

case class Storage(resources: List[ResourceUnit], producers: List[Facility]) {

  def tick(lastAction: Long): Storage = producers.foldRight(this)((f, s) => f.tick(lastAction)(s))

  def add(r: List[ResourceUnit]): Storage =
    r match {
      case x :: xs => add(x); add(xs)
      case _ => this
    }

  def add(r: ResourceUnit): Storage =
    resources.find(x => x.res == r.res) match {
      case Some(v) =>
        Storage(resources.map {
          case x if x.res == r.res => ResourceUnit(x.value + r.value, r.res)
          case x => x
        }, producers)
      case None => Storage(r :: resources, producers)
    }

  def spend(recipe: Recipe): Either[Response, Storage] =
    recipe.cost.find(x => !resources.exists(y => x.res == y.res && y.value >= x.value)) match {
      case Some(v) => Left(Response(s"There isn't enough of ${v.res}"))
      case None =>
        Right(Storage(recipe.cost.foldRight(resources)((a, b) => b.map(bb => bb.res match {
          case a.res => ResourceUnit(bb.value - a.value, a.res)
          case _ => bb
        })), producers))
    }

  //TODO: find a more proper way
  def findOutpost(res: Extractable): Option[Facility] = producers.collect { case x: Outpost => x }.find(x => x.resource.id == res.id)
}
