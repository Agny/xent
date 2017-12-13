package ru.agny.xent.persistence.slick

trait ConfigurableRepository {
  val configPath: String
  val db = CoreInitializer.forConfig(configPath).db
}
