package ru.agny.xent.persistence

import com.typesafe.config.Config

case class DBConfig(
  driver: String,
  url: String,
  user: String,
  password: String
)

object DBConfig {
  def apply(conf: Config): DBConfig = DBConfig(
    conf.getString("driver"),
    conf.getString("url"),
    conf.getString("user"),
    conf.getString("password")
  )
}
