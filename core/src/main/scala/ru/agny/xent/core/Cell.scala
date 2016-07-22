package ru.agny.xent.core

import ru.agny.xent.City
import ru.agny.xent.UserType.UserId

sealed trait Cell {
  val x, y: Int
}
case class WorldCell(x: Int, y: Int, resource: Option[Finite] = None, city: Option[City] = None, owner: Option[UserId] = None) extends Cell
case class LocalCell(x: Int, y: Int, building: Option[Facility] = None) extends Cell

case class CellsMap[T <: Cell](private val cells: Vector[Vector[T]]) {

  def find(c: T): Option[T] = {
    (c.x, c.y) match {
      case (x, y) if (x >= 0 && x < cells.length) && (y >= 0 && y < cells(x).length) => Some(cells(x)(y))
      case _ => None
    }
  }

  def flatMap[A](c: T => Option[A]): List[A] = {
    cells.flatMap(x => rec_flatmap(c)(x.toList, List.empty)).toList
  }

  def filter(c: T => Boolean): List[T] = {
    cells.flatMap(x => rec_filter(c)(x.toList, List.empty)).toList
  }

  private def rec_flatmap[A](c: T => Option[A])(elems: List[T], acc: List[A]): List[A] = {
    elems match {
      case h :: t => c(h) match {
        case Some(v) => rec_flatmap(c)(t, v :: acc)
        case None => rec_flatmap(c)(t, acc)
      }
      case Nil => acc
    }
  }

  private def rec_filter(c: T => Boolean)(elems: List[T], acc: List[T]): List[T] = {
    elems match {
      case h :: t => if (c(h)) rec_filter(c)(t, h :: acc) else rec_filter(c)(t, acc)
      case Nil => acc
    }
  }

  def update(c: T): CellsMap[T] = {
    find(c) match {
      case Some(v) =>
        val yLayer = cells(v.x).updated(v.y, c)
        CellsMap(cells.updated(v.x, yLayer))
      case None => this
    }
  }

}
