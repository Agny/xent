package ru.agny.xent.persistence.slick

import slick.jdbc.PostgresProfile.api._
import ru.agny.xent.core.inventory.Item.ItemId

object ItemStackEntity {
  private lazy val items = ItemTemplateEntity.table
  val table = TableQuery[ItemStackTable]

  class ItemStackTable(tag: Tag) extends Table[ItemStackFlat](tag, "item_stack") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def stackValue = column[Int]("stack")

    def itemId = column[ItemId]("item")

    override def * = (id.?, stackValue, itemId).mapTo[ItemStackFlat]

    def item = foreignKey("item_fk", itemId, items)(_.id, onDelete = ForeignKeyAction.Cascade)
  }
  case class ItemStackFlat(id: Option[Long], stackValue: Int, itemId: ItemId)
}
