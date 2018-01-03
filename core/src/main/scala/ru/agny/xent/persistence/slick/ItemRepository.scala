package ru.agny.xent.persistence.slick

import ru.agny.xent.core.inventory.Item
import ru.agny.xent.core.inventory.Item.ItemId
import ru.agny.xent.persistence.slick.ItemTemplateEntity.ItemFlat
import slick.jdbc.PostgresProfile.api._

case class ItemRepository(configPath: String) extends ConfigurableRepository {

  private val items = ItemTemplateEntity.table

  def create(item: Item) = {
    val insert = items += ItemFlat(item.id, item.weight)
    val result = insert cleanUp {
      case Some(e) => logger.error(e.getMessage); DBIO.failed(e)
      case None => DBIO.successful(1)
    }
    db.run(result)
  }

  def delete(id: ItemId) = {
    val action = items.filter(_.id === id).delete
    db.run(action)
  }

}
