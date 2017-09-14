package ru.agny.xent.core.city

case class Lands(outposts: Vector[Outpost]) {
  def add(outpost: Outpost): Lands = Lands(outpost +: outposts)
}

object Lands {
  val empty = Lands(Vector.empty)
}

