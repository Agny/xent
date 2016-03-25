import sbt._

object Dependencies {

  val json4sJackson = "org.json4s" % "json4s-jackson_2.12.0-M3" % "3.3.0"

  val coreDeps = Seq(json4sJackson)
}