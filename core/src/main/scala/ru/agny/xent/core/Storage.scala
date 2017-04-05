package ru.agny.xent.core

import ru.agny.xent.Response
import ru.agny.xent.core.Item.ItemId

case class Storage(resources: Seq[ResourceUnit]) {

  def tick(lastAction: Long, producers: Seq[Facility]): (Storage, Seq[Facility]) =
    producers.foldLeft(this, Seq.empty[Facility])((s, f) => {
      val (storage, updatedQueue) = f.tick(lastAction)(s._1)
      (storage, s._2 :+ updatedQueue)
    })

  def add(r: Seq[ResourceUnit]): Storage =
    r match {
      case Seq(h, t@_*) => add(h).add(t)
      case _ => this
    }

  def add(r: ResourceUnit): Storage =
    resources.find(x => x.id == r.id) match {
      case Some(v) =>
        Storage(resources.map {
          case x if x.id == r.id => ResourceUnit(x.stackValue + r.stackValue, r.id)
          case x => x
        })
      case None => Storage(r +: resources)
    }

  def spend(recipe: Cost): Either[Response, Storage] =
    recipe.cost.find(x => !resources.exists(y => x.id == y.id && y.stackValue >= x.stackValue)) match {
      case Some(v) => Left(Response(s"There isn't enough of ${v.id}"))
      case None =>
        Right(Storage(recipe.cost.foldRight(resources)((a, b) => b.map(bb => bb.id match {
          case a.id => ResourceUnit(bb.stackValue - a.stackValue, a.id)
          case _ => bb
        }))))
    }

  def get(resource: ItemId): Option[ResourceUnit] = resources.find(_.id == resource)
}

object Storage {
  def empty: Storage = Storage(Seq.empty)
}
