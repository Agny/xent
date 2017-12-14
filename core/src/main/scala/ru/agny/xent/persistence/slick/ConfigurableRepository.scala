package ru.agny.xent.persistence.slick

import com.typesafe.scalalogging.LazyLogging

trait ConfigurableRepository extends LazyLogging {
  val configPath: String
  val db = CoreInitializer.forConfig(configPath).db
}
