import sbt._

object Dependencies {

  val json4sJackson = "org.json4s" % "json4s-jackson_2.11" % "3.4.0"

  val scalactic = "org.scalactic" %% "scalactic" % "3.0.0"
  val scalacticTest = "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  val netty = "io.netty" % "netty-all" % "4.1.4.Final"

  val coreDeps = Seq(json4sJackson, scalactic, scalacticTest)
  val webDeps = Seq(netty)
}