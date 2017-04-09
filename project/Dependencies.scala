import sbt._

object Dependencies {

  val reflect = "org.scala-lang" % "scala-reflect" % "2.11.8"

  val json4sJackson = "org.json4s" % "json4s-jackson_2.11" % "3.4.0"
  val scalactic = "org.scalactic" %% "scalactic" % "3.0.0"
  val scalacticTest = "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  val scredis = "com.livestream" %% "scredis" % "2.0.6"

  val netty = "io.netty" % "netty-all" % "4.1.4.Final"

  val macrosDeps = Vector(reflect)
  val coreDeps = Vector(json4sJackson, scalactic, scalacticTest, scredis)
  val webDeps = Vector(netty)
}