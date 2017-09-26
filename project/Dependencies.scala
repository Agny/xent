import sbt._

object Dependencies {

  val currentScalaVersion = "2.12.2"

  val jmh = "org.openjdk.jmh" % "jmh-generator-annprocess" % "1.19"
  val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % "0.8.0")

  val reflect = "org.scala-lang" % "scala-reflect" % currentScalaVersion
  val parserCombinator = "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"

  val json4sJackson = "org.json4s" %% "json4s-jackson" % "3.5.2"
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  val scalaRedis = "net.debasishg" %% "redisclient" % "3.4"

  val netty = "io.netty" % "netty-all" % "4.1.4.Final"

  val macrosDeps = Vector(reflect, parserCombinator)
  val coreDeps = Vector(json4sJackson, scalatest, scalaRedis)
  val benchDeps = Vector(jmh) ++ circe
  val webDeps = Vector(netty)
}