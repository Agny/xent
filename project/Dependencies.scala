import sbt._

object Dependencies {

  val Postgre = Seq(
    "org.postgresql" % "postgresql" % "42.2.23"
  )

  val Doobie = Seq()
//    "org.tpolecat" % "doobie-core_3.0.0-RC1",
//    "org.tpolecat" % "doobie-hikari_3.0.0-RC1",
//    "org.tpolecat" % "doobie-postgres_3.0.0-RC1"
//  ) map (_ % "0.12.0" excludeAll("org.typelevel", "*"))

  val Kafka = Seq(
    "org.apache.kafka" % "kafka-clients" % "2.5.0"
  )
  val Testing = Seq(
    "org.scalatest" %% "scalatest" % "3.2.9" % Test,
    "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.39.5" % Test,
    "com.dimafeng" %% "testcontainers-scala-kafka" % "0.39.5" % Test
  )
  val Circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-jawn",
    "io.circe" %% "circe-numbers",
    "io.circe" %% "circe-parser"
  ).map(_ % "0.14.1")
  val Cats = Seq(
    "org.typelevel" %% "cats-core" % "2.6.1",
    "org.typelevel" %% "cats-effect" % "3.2.2"
  )
  val Config = Seq(
    "com.typesafe" % "config" % "1.4.1"
  )
  val Logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.5"
  )
}
