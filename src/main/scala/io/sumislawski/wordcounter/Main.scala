package io.sumislawski.wordcounter

import cats.data.ValidatedNel
import cats.effect.{ExitCode, IO, Resource}
import cats.syntax.all._
import com.comcast.ip4s.{IpLiteralSyntax, Port}
import com.monovore.decline.effect.CommandIOApp
import com.monovore.decline.{Argument, Opts}
import io.sumislawski.wordcounter.core.WordCounter
import io.sumislawski.wordcounter.infrastructure.eventsource.ExecutableFileEventSource
import io.sumislawski.wordcounter.infrastructure.httpapi.{HttpServer, WordCounterRoutes}
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.nio.file.Path
import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Main extends CommandIOApp(
  name = "word-counter",
  header = "Word counter"
) {

  private val logger = Slf4jLogger.getLogger[IO]


  override def main: Opts[IO[ExitCode]] = {
    (
      Opts.argument[Path]("executable file path"),
      Opts.option[FiniteDuration]("timeWindow", "Length of the time window in which the words are counted", short = "w").withDefault(15.seconds),
      Opts.option[Port]("httpPort", "TCP port for the HTTP server to listen on", "p").withDefault(port"80"),
      ).mapN { (executableFilePath, timeWindow, httpPort) =>
      for {
        _ <- logger.info("Starting word counter.")
        _ <- service(executableFilePath, timeWindow, httpPort).useForever
      } yield ExitCode.Success
    }
  }

  private def service(executableFilePath: Path, timeWindow: FiniteDuration, httpPort: Port): Resource[IO, Unit] = for {
    eventSource <- Resource.pure(new ExecutableFileEventSource[IO](executableFilePath))
    wordCounter <- WordCounter[IO](eventSource, timeWindow = timeWindow)
    routes <- Resource.pure(new WordCounterRoutes[IO](wordCounter))
    _ <- HttpServer[IO](routes.routes, httpPort)
  } yield ()

  private implicit val portArgument: Argument[Port] = new Argument[Port] {
    override def read(string: String): ValidatedNel[String, Port] =
      Port.fromString(string).toValidNel(s"Invalid port: $string")

    override def defaultMetavar: String = "port"
  }

}
