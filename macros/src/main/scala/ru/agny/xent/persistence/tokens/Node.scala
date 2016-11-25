package ru.agny.xent.persistence.tokens

trait Node

trait Primitive[T] extends Node {
  val k: T
}
case class StringValue(k: String) extends Primitive[String]
case class IntValue(k: Int) extends Primitive[Int]
case class LongValue(k: Long) extends Primitive[Long]
case class DoubleValue(k: Double) extends Primitive[Double]
