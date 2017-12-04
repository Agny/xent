package ru.agny.xent.persistence.slick

import DefaultProfile.api._
import ru.agny.xent.core.inventory.Item.{ItemId, ItemWeight}
import ru.agny.xent.core.inventory.ItemStack

object ItemStackEntity {
  lazy val table = TableQuery[ItemStackTable]

  class ItemStackTable(tag: Tag) extends Table[ItemStackFlat](tag, "item_stack") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def stackValue = column[Int]("stack")

    def itemId = column[ItemId]("item")

    def singleWeight = column[ItemWeight]("weight")

    override def * = (id, stackValue, itemId, singleWeight).mapTo[ItemStackFlat]
  }
  case class ItemStackFlat(id: Long, stackValue: Int, itemId: ItemId, singleWeight: ItemWeight) {
    def toItemStack: ItemStack = ItemStack(stackValue, itemId, singleWeight)
  }
}
