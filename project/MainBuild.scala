import sbt._
import Keys._
import Dependencies._

object MainBuild extends Build {

  override lazy val settings = super.settings ++ Seq(
    scalaVersion := currentScalaVersion
  )

  lazy val commonSettings = Vector(
    organization := "ru.agny",
    version := "0.1.0",
    //scalacOptions ++= Seq("-feature"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )

  lazy val macros = project.settings(commonSettings: _*).settings(libraryDependencies ++= macrosDeps)

  lazy val core = project.settings(commonSettings: _*).settings(libraryDependencies ++= coreDeps).dependsOn(macros)

  lazy val web = project.settings(commonSettings: _*).settings(libraryDependencies ++= webDeps).dependsOn(core)

  lazy val root = project.in(file(".")).settings(commonSettings: _*).aggregate(web)
}