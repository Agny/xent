package ru.agny.xent.persistence.tokens

trait Node

trait Primitive[T] extends Node {
  val k: T
}
case class StringValue(k: String) extends Primitive[String]
case class LongValue(k: Long) extends Primitive[Long]
case class DoubleValue(k: Double) extends Primitive[Double]
//case object NoneValue extends Primitive[Nothing] {
//  val k = Nothing
//}
