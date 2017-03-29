package ru.agny.xent.core.utils

import ru.agny.xent.core.Item.ItemId

import scala.collection.mutable

object IdHolder {
  private val ids = mutable.Map[String, ItemId]()

  //TODO There must be a key?
  def get(name: String): ItemId = ids(name)

  def get(id: ItemId): String = ids.find(i => i._2 == id).get._1

  def add(key: String, id: ItemId): ItemId = {
    ids += (key -> id)
    id
  }
}
