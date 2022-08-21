package io.sumislawski.wordcounter.core

import cats.Applicative
import cats.effect.{Async, Resource}
import cats.effect.syntax.all._

import scala.concurrent.duration.FiniteDuration

class WordCounter[F[_] : Applicative] {

  def getWordCountByEventInTimeWindow(): F[Map[EventType, Long]] =
    Applicative[F].pure(Map.empty)

}

object WordCounter {
  def apply[F[_] : Async](eventSource: EventSource[F], timeWindow: FiniteDuration): Resource[F, WordCounter[F]] = for {
    _ <- eventSource.events.compile.drain.background // TODO process the events
  } yield new WordCounter[F]
}
