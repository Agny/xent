package ru.agny.xent.persistence.slick

import slick.jdbc.PostgresProfile.api._

case class ItemStackRepository(configPath: String) extends ConfigurableRepository {
  private val items = ItemStackEntity.table

  def clean() = db.run(items.delete)
}
