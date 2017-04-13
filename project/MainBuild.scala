import sbt._
import Keys._
import Dependencies._

object Resolvers {
  val artimarepo = "Artima Maven Repository" at "http://repo.artima.com/releases"
  val sonatype = "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases/"

  val scalatest = Vector(artimarepo)
  val scredis = Vector(sonatype)
}

object MainBuild extends Build {
  import Resolvers._
  lazy val commonSettings = Vector(
    organization := "ru.agny",
    version := "0.1.0",
    scalaVersion := "2.11.8",
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )

  lazy val macros = project.settings(commonSettings: _*).settings(libraryDependencies ++= macrosDeps)

  lazy val core = project.settings(commonSettings: _*).settings(libraryDependencies ++= coreDeps).dependsOn(macros)

  lazy val web = project.settings(commonSettings: _*).settings(libraryDependencies ++= webDeps).dependsOn(core)

  lazy val root = project.in(file(".")).settings(resolvers := (scalatest ++ scredis)).settings(commonSettings: _*).aggregate(web)
}