import sbt._

object Dependencies {

  val Postgre = Seq(
    "org.postgresql" % "postgresql" % "42.2.16"
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
    "org.scalatest" %% "scalatest" % "3.2.7" % Test,
    "org.scalamock" % "scalamock_2.13" % "5.1.0" % Test,
    "com.dimafeng" % "testcontainers-scala-scalatest_2.13" % "0.38.1" % Test,
    "com.dimafeng" % "testcontainers-scala-kafka_2.13" % "0.38.1" % Test
  )
  val Circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-jawn",
    "io.circe" %% "circe-numbers",
    "io.circe" %% "circe-parser"
  ).map(_ % "0.14.0-M5")
  val Cats = Seq(
    "org.typelevel" %% "cats-core" % "2.5.0",
    "org.typelevel" %% "cats-effect" % "3.0.2"
  )
  val Config = Seq(
    "com.typesafe" % "config" % "1.4.0"
  )
  val Logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
}
