package ru.agny.xent.core

import ru.agny.xent.{ResourceUnit, Response}

case class Storage(resources: Seq[ResourceUnit], producers: Seq[Facility]) {

  def tick(lastAction: Long): Storage = producers.foldRight(this)((f, s) => f.tick(lastAction)(s))

  def add(r: Seq[ResourceUnit]): Storage =
    r match {
      case Seq(h, t@_*) => add(h); add(t)
      case _ => this
    }

  def add(r: ResourceUnit): Storage =
    resources.find(x => x.res == r.res) match {
      case Some(v) =>
        Storage(resources.map {
          case x if x.res == r.res => ResourceUnit(x.value + r.value, r.res)
          case x => x
        }, producers)
      case None => Storage(r +: resources, producers)
    }

  def add(facility: Facility) = this.copy(producers = facility +: producers)

  def spend(recipe: Cost): Either[Response, Storage] =
    recipe.cost.find(x => !resources.exists(y => x.res == y.res && y.value >= x.value)) match {
      case Some(v) => Left(Response(s"There isn't enough of ${v.res}"))
      case None =>
        Right(Storage(recipe.cost.foldRight(resources)((a, b) => b.map(bb => bb.res match {
          case a.res => ResourceUnit(bb.value - a.value, a.res)
          case _ => bb
        })), producers))
    }

  def get(resource: String):Option[ResourceUnit] = resources.find(_.res == resource)
}

object Storage {
  def empty: Storage = Storage(Seq.empty, Seq.empty)
}
