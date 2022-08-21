package io.sumislawski.wordcounter

import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Main extends IOApp {

  private val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- logger.info("Starting word counter.")
  } yield ExitCode.Success

}
