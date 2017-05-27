package ru.agny.xent.core.utils

import scala.collection.IndexedSeq._
import scala.collection.IndexedSeq
import scala.collection.generic.GenericTraversableTemplate

case class NESeq[A](override val head: A, override val tail: IndexedSeq[A]) extends IndexedSeq[A]
  with GenericTraversableTemplate[A, IndexedSeq] {

  private val underlying = head +: tail

  override def apply(idx: Int): A = underlying(idx)

  def length = underlying.length
}

object NESeq {
  def apply[A](underlying: IndexedSeq[A]): NESeq[A] = NESeq(underlying.head, underlying.tail)
}
