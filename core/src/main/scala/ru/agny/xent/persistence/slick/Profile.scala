package ru.agny.xent.persistence.slick

import slick.jdbc.JdbcProfile

trait Profile {
  val profile: JdbcProfile
}

object DefaultProfile extends Profile {
  override val profile = slick.jdbc.PostgresProfile
  val api = profile.api
  val db = api.Database.forConfig("db")
}
