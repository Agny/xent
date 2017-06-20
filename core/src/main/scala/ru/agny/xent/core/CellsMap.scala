package ru.agny.xent.core

import scala.annotation.tailrec

case class CellsMap[T <: Cell](private val cells: Vector[Vector[T]]) {
  val length = cells.length

  def find[A <: Cell](c: A): Option[T] = {
    (c.x, c.y) match {
      case (x, y) if (x >= 0 && x < length) && (y >= 0 && y < length) => Some(cells(x)(y))
      case _ => None
    }
  }

  def flatMap[A](c: T => Option[A]): Vector[A] = {
    cells.flatMap(x => rec_flatmap(c)(x, Vector.empty))
  }

  def filter(c: T => Boolean): Vector[T] = {
    cells.flatMap(x => rec_filter(c)(x, Vector.empty))
  }

  @tailrec
  private def rec_flatmap[A](c: T => Option[A])(elems: Vector[T], acc: Vector[A]): Vector[A] = {
    elems match {
      case h +: t => c(h) match {
        case Some(v) => rec_flatmap(c)(t, v +: acc)
        case None => rec_flatmap(c)(t, acc)
      }
      case Vector() => acc
    }
  }


  private def rec_filter(c: T => Boolean)(elems: Vector[T], acc: Vector[T]): Vector[T] = {
    elems match {
      case h +: t => if (c(h)) rec_filter(c)(t, h +: acc) else rec_filter(c)(t, acc)
      case Vector() => acc
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

  //TODO extract visible range parameters
  private val xScreen = 7
  private val yScreen = 5

  def view(x: Int, y: Int) = {
    val (fx, tx) = getRange(x, xScreen)
    val (fy, ty) = getRange(y, yScreen)
    val xInRange = isInRange(fx, tx) _
    val yInRange = isInRange(fy, ty) _
    filter(c => xInRange(c.x) && yInRange(c.y))
  }

  private def isInRange(from: Int, to: Int)(i: Int) =
    i >= (from % length + length) % length &&
      i <= (to % length + length) % length

  private def getRange(i: Int, range: Int) = {
    (i - range / 2, i + range / 2)
  }

}

