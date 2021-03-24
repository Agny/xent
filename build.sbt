import Dependencies._

ThisBuild / organization := "ru.agny"
ThisBuild / version := "0.2.0"
ThisBuild / scalaVersion := "3.0.0-RC1"
ThisBuild / crossPaths := false

//val macros = project.in(file("macros"))
//  .settings(commonSettings, libraryDependencies ++= macrosDeps)

val common = project.in(file("common"))
  .settings(libraryDependencies ++= Cats ++ Circe ++ Config ++ Kafka ++ Logging ++ Testing)

val core = project.in(file("core"))
  .dependsOn(common % "compile->compile;test->test")
  .enablePlugins(JmhPlugin)
//  .dependsOn(macros)

//val shared = project.in(file("shared"))
//  .settings(commonSettings)

//val market = project.in(file("market"))
//  .settings(commonSettings, libraryDependencies ++= marketDeps)
//  .dependsOn(core % "compile->compile;test->test", shared)

//val web = project.in(file("web"))
//  .settings(commonSettings, libraryDependencies ++= webDeps)
//  .settings(mainClass in assembly := Some("ru.agny.xent.web.Basic"))
//  .dependsOn(core, shared)
