package ru.agny.xent.persistence.tokens

trait Node[T] {
  def from(v: String): T
}

object Nodes {
  implicit object StringValue extends Node[String] {
    override def from(v: String) = v
  }
  implicit object IntValue extends Node[Int] {
    override def from(v: String) = v.toInt
  }
  implicit object LongValue extends Node[Long] {
    override def from(v: String) = v.toLong
  }
  implicit object DoubleValue extends Node[Double] {
    override def from(v: String) = v.toDouble
  }
}

case class Token(v: String) {
  def materialize[T](implicit node: Node[T]): T = node.from(v)
}
