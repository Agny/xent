import sbt._

object Dependencies {

  val currentScalaVersion = "2.12.4"
  val akkaVersion = "2.5.6"

  val reflect = "org.scala-lang" % "scala-reflect" % currentScalaVersion
  val parserCombinator = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"

  val circe = Vector("io.circe" %% "circe-core", "io.circe" %% "circe-generic", "io.circe" %% "circe-parser").map(_ % "0.8.0")
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.0" % Test
  val scalaRedis = "net.debasishg" %% "redisclient" % "3.4"

  val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
  )
  val slick = Seq(
    "com.typesafe.slick" %% "slick" % "3.2.1",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.2.1"
  )
  val postgre = "org.postgresql" % "postgresql" % "42.1.4"

  val netty = "io.netty" % "netty-all" % "4.1.4.Final"

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion

  val akkaHttp = Seq(
    "com.typesafe.akka" %% "akka-http" % "10.0.10",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.0.10" % Test
  )

  val macrosDeps = Vector(reflect, parserCombinator)
  val coreDeps = Vector(scalatest, scalaRedis, akkaActor, postgre) ++ circe ++ slick ++ logging
  val webDeps = Vector(netty)
  val marketDeps = akkaHttp
}