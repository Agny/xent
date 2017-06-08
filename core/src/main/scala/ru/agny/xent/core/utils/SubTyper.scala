package ru.agny.xent.core.utils

trait SubTyper[From, To] {
  def asSub(a: From): Option[To]
}

object SubTyper {

  def partition[A <: B, B, C](f: Vector[(C, B)])(implicit ev: SubTyper[B, A]): (Vector[(C, A)], Vector[(C, B)]) =
    f.foldLeft((Vector.empty[(C, A)], Vector.empty[(C, B)]))((s, x) => {
      ev.asSub(x._2) match {
        case Some(a) => ((x._1, a) +: s._1, s._2)
        case _ => (s._1, x +: s._2)
      }
    })

  def partition[A <: C, B <: C, C](f: Vector[C])(implicit ev1: SubTyper[C, A], ev2: SubTyper[C, B]): (Vector[A], Vector[B]) =
    f.foldLeft((Vector.empty[A], Vector.empty[B]))((s, x) => {
      (ev1.asSub(x), ev2.asSub(x)) match {
        case (Some(a), _) => (a +: s._1, s._2)
        case (_, Some(b)) => (s._1, b +: s._2)
        case _ => s
      }
    })
}
