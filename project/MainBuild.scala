import sbt._
import Keys._
import Dependencies._

object MainBuild extends Build {

  lazy val commonSettings = Seq(
    organization := "ru.agny",
    version := "0.1.0",
    scalaVersion := "2.12.0-M3"
  )

  lazy val core = project.settings(commonSettings: _*).settings(libraryDependencies ++= coreDeps)

  lazy val web = project.settings(commonSettings: _*)

  lazy val root = project.in(file(".")).settings(commonSettings: _*).aggregate(core, web)
}