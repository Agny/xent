package ru.agny.xent.core.utils

case class NEVector[A](override val head: A, override val tail: Vector[A]) extends Traversable[A] {
  private val iterator = (head +: tail).iterator

  override def foreach[U](f: (A) => U): Unit = iterator.foreach(f)
}
