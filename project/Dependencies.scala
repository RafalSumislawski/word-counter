import sbt._

object Dependencies{
  object logging {
    val log4jVersion = "2.18.0"
    val log4jApi = "org.apache.logging.log4j" % "log4j-api" % log4jVersion
    val log4jSlf4j = "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion
    val lmaxDisruptor = "com.lmax" % "disruptor" % "3.4.4"
    val log4cats = "org.typelevel" %% "log4cats-slf4j" % "2.4.0"
    val all = Vector(log4jApi, log4jSlf4j, lmaxDisruptor, log4cats)
  }

  object cats {
    val catsCore = "org.typelevel" %% "cats-core" % "2.8.0"
    val catsEffect = "org.typelevel" %% "cats-effect" % "3.3.14"
    val all = Vector(catsCore, catsEffect)
  }

  object circe {
    val circeVersion = "0.14.2"
    val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
    val circeParser = "io.circe" %% "circe-parser" % circeVersion
    val circeLiteral = "io.circe" %% "circe-literal" % circeVersion
    val all = Vector(circeGeneric, circeParser, circeLiteral)
  }

  object http4s {
    val http4sVersion = "0.23.14"
    val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
    val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion
    val blazeVersion = "0.23.12"
    val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % blazeVersion
    val all = Vector(http4sDsl, http4sCirce, http4sBlazeServer)
  }

  val fs2Io = "co.fs2" %% "fs2-io" % "3.2.12"

  object scalaTest {
    val scalaTest = "org.scalatest" %% "scalatest" % "3.2.13"
    val catsEffectTestingScalaTest = "org.typelevel" %% "cats-effect-testing-scalatest" % "1.4.0"

    val all = Vector(scalaTest, catsEffectTestingScalaTest)
  }

}