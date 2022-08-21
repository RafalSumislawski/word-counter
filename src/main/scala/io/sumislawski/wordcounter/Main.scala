package io.sumislawski.wordcounter

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.syntax.all._
import io.sumislawski.wordcounter.core.WordCounter
import io.sumislawski.wordcounter.infrastructure.eventsource.ExecutableFileEventSource
import io.sumislawski.wordcounter.infrastructure.httpapi.{HttpServer, WordCounterRoutes}
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.net.InetSocketAddress
import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  private val logger = Slf4jLogger.getLogger[IO]

  override def run(args: List[String]): IO[ExitCode] = for {
    _ <- logger.info("Starting word counter.")
    customBindAddress <- args.headOption.traverse(s => IO(new InetSocketAddress(s.toInt)))
    bindAddress = customBindAddress.getOrElse(new InetSocketAddress(80))
    _ <- service(bindAddress).useForever
  } yield ExitCode.Success

  private def service(bindAddress: InetSocketAddress): Resource[IO, Unit] = for {
    eventSource <- Resource.pure(new ExecutableFileEventSource[IO]())
    wordCounter <- WordCounter[IO](eventSource, timeWindow = 15.seconds)
    routes <- Resource.pure(new WordCounterRoutes[IO](wordCounter))
    _ <- HttpServer[IO](routes.routes, bindAddress)
  } yield ()

}
