import sbt._
import Keys._

object MainBuild extends Build {

  lazy val commonSettings = Seq(
    organization := "ru.agny",
    version := "0.1.0",
    scalaVersion := "2.11.7"
  )

  lazy val root = project.in(file(".")).settings(commonSettings: _*).aggregate(core, web)

  lazy val core = project.settings(commonSettings: _*)

  lazy val web = project.settings(commonSettings: _*)
}