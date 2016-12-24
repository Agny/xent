package ru.agny.xent.persistence.tokens

trait Node[T]

object Nodes {
  implicit object StringValue extends Node[String]
  implicit object IntValue extends Node[Int]
  implicit object LongValue extends Node[Long]
  implicit object DoubleValue extends Node[Double]
}

case class Primitive(v: String) {

  import Nodes._

  def materialize[T](implicit to: Node[T]): T = to match {
    case StringValue => v.asInstanceOf[T]
    case LongValue => v.toLong.asInstanceOf[T]
    case DoubleValue => v.toDouble.asInstanceOf[T]
    case IntValue => v.toInt.asInstanceOf[T]
  }
}
