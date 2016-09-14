import sbt._
import Keys._
import Dependencies._

object Resolvers {
  val artimarepo = "Artima Maven Repository" at "http://repo.artima.com/releases"

  val scalatest = Seq(artimarepo)
}

object MainBuild extends Build {
  import Resolvers._
  lazy val commonSettings = Seq(
    organization := "ru.agny",
    version := "0.1.0",
    scalaVersion := "2.11.8"
  )

  lazy val core = project.settings(commonSettings: _*).settings(libraryDependencies ++= coreDeps)

  lazy val web = project.settings(commonSettings: _*).settings(libraryDependencies ++= webDeps).dependsOn(core)

  lazy val root = project.in(file(".")).settings(resolvers := scalatest).settings(commonSettings: _*).aggregate(web)
}