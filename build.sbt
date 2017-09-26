import Dependencies._

val commonSettings = Seq(
  organization := "ru.agny",
  version := "0.1.0",
  scalaVersion := currentScalaVersion,
  scalacOptions ++= Seq("-language:implicitConversions", "-language:postfixOps", "-feature"),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

val macros = project.in(file("macros"))
  .settings(commonSettings, libraryDependencies ++= macrosDeps)

val core = project.in(file("core"))
  .settings(commonSettings, libraryDependencies ++= coreDeps)
  .dependsOn(macros)

val bench = project.in(file("bench"))
  .settings(commonSettings, libraryDependencies ++= benchDeps)
  .dependsOn(core)

val web = project.in(file("web"))
  .settings(commonSettings, libraryDependencies ++= webDeps)
  .settings(mainClass in assembly := Some("ru.agny.xent.web.Basic"))
  .dependsOn(core)

lazy val root = project.in(file("."))
  .aggregate(web, core)
  .settings(commonSettings: _*)