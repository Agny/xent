import sbt._

object Dependencies {

  val Kafka = Seq(
    "org.apache.kafka" % "kafka-clients" % "2.5.0"
  )
  val Testing = Seq(
    "org.scalatest" % "scalatest_2.13" % "3.2.0" % Test,
    "com.dimafeng" % "testcontainers-scala-scalatest_2.13" % "0.38.1" % Test,
    "com.dimafeng" % "testcontainers-scala-kafka_2.13" % "0.38.1" % Test
  )
  val Circe = Seq(
    "io.circe" % "circe-core_2.13",
    "io.circe" % "circe-generic_2.13",
    "io.circe" % "circe-jawn_2.13",
    "io.circe" % "circe-numbers_2.13",
    "io.circe" % "circe-parser_2.13",
  ).map(_ % "0.13.0" exclude("org.typelevel", "cats-core_2.13"))
  val Cats = Seq(
    "org.typelevel" % "cats-core_2.13" % "2.2.0-RC2",
    "org.typelevel" % "cats-effect_2.13" % "2.1.4"
  )
  val Config = Seq(
    "com.typesafe" % "config" % "1.4.0"
  )
  val Logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3"
  )
}
