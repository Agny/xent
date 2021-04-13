package ru.agny.xent.item

case class Storage(
  private var resources: Map[Resource, Int]
) {
  def add(v: ItemStack): Unit = {
    resources = resources.updatedWith(v.r) {
      case Some(x) => Some(x + v.volume)
      case None => Some(v.volume)
    }
  }
}

object Storage {
  val Empty = Storage(Map.empty)
}
