package ru.agny.xent.trade

import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.persistence.slick.ItemStackEntity.ItemStackFlat

case class ItemHolder(id: ItemId, amount: Int)

object ItemHolder {
  implicit def toItemHolder(item: ItemStackFlat): ItemHolder = ItemHolder(item.itemId, item.stackValue)
}