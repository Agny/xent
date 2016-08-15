import sbt._

object Dependencies {

  val json4sJackson = "org.json4s" % "json4s-jackson_2.12.0-M3" % "3.3.0"
  val netty = "io.netty" % "netty-all" % "4.1.4.Final"

  val coreDeps = Seq(json4sJackson)
  val webDeps = Seq(netty)
}