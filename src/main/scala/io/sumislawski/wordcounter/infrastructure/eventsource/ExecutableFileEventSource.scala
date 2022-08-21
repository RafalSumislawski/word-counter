package io.sumislawski.wordcounter.infrastructure.eventsource

import cats.effect.std.Queue
import cats.effect.{Async, Resource}
import cats.syntax.all._
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.github.vigoo.prox.ProxFS2
import io.sumislawski.wordcounter.core.{Event, EventSource, EventType, Timestamp}
import io.sumislawski.wordcounter.infrastructure.eventsource.ExecutableFileEventSource._
import org.typelevel.log4cats.slf4j.Slf4jLogger

class ExecutableFileEventSource[F[_] : Async](executableFilePath: String) extends EventSource[F] {

  private val logger = Slf4jLogger.getLogger[F]

  private val prox = ProxFS2[F]

  import prox.{JVMProcessInfo, JVMProcessRunner, OutputStreamToSink, Process, ProcessRunner}

  private implicit val runner: ProcessRunner[JVMProcessInfo] = new JVMProcessRunner

  override def events: fs2.Stream[F, Event] = for {
    eventQueue <- fs2.Stream.resource(executeProcess(executableFilePath))
    event <- fs2.Stream.fromQueueUnterminated(eventQueue)
  } yield event

  private def executeProcess(command: String): Resource[F, Queue[F, Event]] = for {
    eventQueue <- Resource.eval(Queue.bounded[F, Event](1024))
    _ <- Process(command)
      .connectOutput(OutputStreamToSink(s => processOutput(s).enqueueUnterminated(eventQueue)))
      .start()
  } yield eventQueue

  private def processOutput(s: fs2.Stream[F, Byte]): fs2.Stream[F, Event] =
    s.through(fs2.text.utf8.decode)
      .through(fs2.text.lines)
      .evalMap(parseOutputLine)
      .flattenOption

  private def parseOutputLine(line: String): F[Option[Event]] =
    io.circe.parser.parse(line).flatMap(_.as[Event]) match {
      case Right(event) =>
        logger.info(s"Received [$event].")
          .as(Some(event))
      case Left(t) =>
        logger.warn(s"Ignoring a malformed event. [$t]")
          .as(None)
    }

}

object ExecutableFileEventSource {

  private implicit val circeConfiguration: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit val eventDecoder: Decoder[Event] = deriveConfiguredDecoder

  implicit val eventTypeDecoder: Decoder[EventType] = Decoder.decodeString.map(EventType)

  implicit val timestampDecoder: Decoder[Timestamp] = Decoder.decodeLong.map(Timestamp(_))

}