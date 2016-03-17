package ru.agny.xent

sealed trait Resource {
  val name: String
  val since: Set[Prereq]
  val defaultYield = 1

  def out(): ResourceUnit

  def out(amount: Int): ResourceUnit = {
    def recursive_out(amount: Int, acc: List[ResourceUnit]): List[ResourceUnit] = {
      if (amount == 1) out() :: acc
      else recursive_out(amount - 1, out() :: acc)
    }
    recursive_out(amount, List.empty).foldRight(ResourceUnit(0, this))((x, y) => ResourceUnit(x.value + y.value, x.res))
  }

  override def toString = s"$name"
}
case class Extractable(id: Long, name: String, volume: Int, since: Set[Prereq]) extends Resource {
  override def out(): ResourceUnit =
    if (defaultYield > volume) ResourceUnit(0, this)
    else ResourceUnit(defaultYield, this.copy(volume = volume - defaultYield))

  override def toString = s"$name[$volume]"
}
case class Producible(name: String, recipe: Recipe, since: Set[Prereq]) extends Resource {
  override def out(): ResourceUnit = ResourceUnit(defaultYield, this)

}

case class Recipe(cost: List[ResourceUnit])
case class ResourceUnit(value: Int, res: Resource)