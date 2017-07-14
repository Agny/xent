import sbt._

object Dependencies {

  val currentScalaVersion = "2.12.2"

  val reflect = "org.scala-lang" % "scala-reflect" % currentScalaVersion

  val json4sJackson = "org.json4s" %% "json4s-jackson" % "3.5.2"
  val scalatest = "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  val scalaRedis = "net.debasishg" %% "redisclient" % "3.4"

  val netty = "io.netty" % "netty-all" % "4.1.4.Final"

  val macrosDeps = Vector(reflect)
  val coreDeps = Vector(json4sJackson, scalatest, scalaRedis)
  val webDeps = Vector(netty)
}