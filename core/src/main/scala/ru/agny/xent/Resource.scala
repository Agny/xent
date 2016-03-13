package ru.agny.xent

sealed trait Resource {
  val name: String
  val since: Set[Prereq]
  val defaultYield = 1

  def out(): (Int, Resource)

  def out(amount: Int): (Int, Resource) = {
    def recursive_out(amount: Int, acc: List[(Int, Resource)]): List[(Int, Resource)] = {
      if (amount == 1) out() :: acc
      else recursive_out(amount - 1, out() :: acc)
    }
    val sum = recursive_out(amount, List.empty).foldRight(0)((x, y) => x._1 + y)
    (sum, this)
  }

  override def toString = name
}
case class Extractable(name: String, volume: Int, since: Set[Prereq]) extends Resource {
  override def out(): (Int, Resource) =
    if (defaultYield > volume) (0, this)
    else (defaultYield, this.copy(volume = volume - defaultYield))
}
case class Producible(name: String, recipe: Recipe, since: Set[Prereq]) extends Resource {
  override def out(): (Int, Resource) = (defaultYield, this)
}

case class Recipe(cost:Map[Int,Resource])