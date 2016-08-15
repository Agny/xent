import sbt._
import Keys._
import Dependencies._

object MainBuild extends Build {

  lazy val commonSettings = Seq(
    organization := "ru.agny",
    version := "0.1.0",
    scalaVersion := "2.12.0-M3"
  )

  lazy val common = project.settings(commonSettings: _*).settings(libraryDependencies ++= commonDeps)

  lazy val core = project.settings(commonSettings: _*).settings(libraryDependencies ++= coreDeps).dependsOn(common)

  lazy val web = project.settings(commonSettings: _*).settings(libraryDependencies ++= webDeps).dependsOn(common)

  lazy val root = project.in(file(".")).settings(commonSettings: _*).aggregate(core, web)
}