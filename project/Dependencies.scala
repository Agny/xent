import sbt._

object Dependencies {

  val json4sJackson = "org.json4s" % "json4s-jackson_2.11" % "3.4.0" exclude("org.scala-lang.modules", "scala-xml_2.11")

  val scalactic = "org.scalactic" %% "scalactic" % "3.0.0"
  val scalacticTest = "org.scalatest" %% "scalatest" % "3.0.0" % "test"
  val netty = "io.netty" % "netty-all" % "4.1.4.Final"

  val coreDeps = Seq(json4sJackson)
  val webDeps = Seq(netty)
}