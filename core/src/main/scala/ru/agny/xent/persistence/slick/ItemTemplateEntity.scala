package ru.agny.xent.persistence.slick

import ru.agny.xent.core.inventory.Item.{ItemId, ItemWeight}
import slick.jdbc.PostgresProfile.api._

object ItemTemplateEntity {

  val table = TableQuery[ItemTable]

  class ItemTable(tag: Tag) extends Table[ItemFlat](tag, "item") {
    def id = column[ItemId]("id", O.PrimaryKey)

    def weight = column[ItemWeight]("weight")

    override def * = (id, weight).mapTo[ItemFlat]
  }
  case class ItemFlat(id: ItemId, weight: ItemWeight)

}
