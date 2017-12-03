package ru.agny.xent.persistence.slick

import DefaultProfile.api._
import ru.agny.xent.core.inventory.ItemStack
import ru.agny.xent.core.inventory.Item.{ItemId, ItemWeight}

object ItemStackEntity {
  lazy val table = TableQuery[ItemStackTable]

  class ItemStackTable(tag: Tag) extends Table[ItemStack](tag, "item_stack") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def stackValue = column[Int]("stack")

    def itemId = column[ItemId]("item")

    def singleWeight = column[ItemWeight]("weight")

    override def * = (stackValue, itemId, singleWeight).mapTo[ItemStack]
  }
}
