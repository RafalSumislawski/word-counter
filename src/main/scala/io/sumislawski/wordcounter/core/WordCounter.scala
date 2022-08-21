package io.sumislawski.wordcounter.core

import cats.Applicative
import cats.effect.{Async, Resource}

import scala.concurrent.duration.FiniteDuration

class WordCounter[F[_] : Applicative] {

  def getWordCountByEventInTimeWindow(): F[Map[EventType, Long]] =
    Applicative[F].pure(Map.empty)

}

object WordCounter {
  def apply[F[_] : Async](eventSource: EventSource[F], timeWindow: FiniteDuration): Resource[F, WordCounter[F]] = for {
    _ <- Resource.unit[F]
  } yield new WordCounter[F]
}
