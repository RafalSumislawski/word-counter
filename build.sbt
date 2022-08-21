import Dependencies._
import _root_.io.github.davidgregory084.DevMode

name := "word-counter"
organization := "io.sumislawski"
version := "0.1.0-SNAPSHOT"

scalaVersion := "2.13.8"

libraryDependencies ++= logging.all ++ cats.all ++ circe.all ++ http4s.all ++ scalaTest.all.map(_ % Test) ++ Vector(fs2Io, prox, decline)

ThisBuild / tpolecatDefaultOptionsMode := DevMode
ThisBuild / tpolecatExcludeOptions := Set(ScalacOptions.warnDeadCode)

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
