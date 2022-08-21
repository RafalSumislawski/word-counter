package io.sumislawski.wordcounter.core

import cats.effect._
import cats.effect.syntax.all._
import cats.syntax.all._
import io.sumislawski.wordcounter.core.WordCounter.{EventWordCount, WindowedCounter}
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.Instant
import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.concurrent.duration.FiniteDuration

class WordCounter[F[_] : Sync] private(state: Ref[F, Map[EventType, WindowedCounter]], timeWindow: java.time.Duration) {

  private val logger = Slf4jLogger.getLogger[F]

  def getWordCountByEventInTimeWindow(): F[Map[EventType, Long]] =
    for {
      deadline <- currentDeadline()
      counters <- state.updateAndGet { current =>
        current.flatMap { case (eventType, counter) =>
          val updatedCounter = counter.dropEventsOlderThan(deadline)
          if (updatedCounter.wordCount == 0) List()
          else List(eventType -> updatedCounter)
        }
      }
      counts = counters.view.mapValues(_.wordCount).toMap
      _ <- logger.info(s"Returning current word counts: [$counts]")
    } yield counts

  private def process(event: Event): F[Unit] =
    for {
      deadline <- currentDeadline()
      eventWordCount = countWords(event)
      _ <- logger.info(s"Processing event [$event] with [${eventWordCount.wordCount}] words.")
      _ <- state.update { current =>
        val updatedCounter = current.getOrElse(event.eventType, WindowedCounter.empty)
          .add(eventWordCount)
          // Theoretically we don't need to drop the old events here, we will do it in getWordCountByEventInTimeWindow, but...
          // that would mean adding new events without removing old ones and hoping that someone will call getWordCountByEventInTimeWindow before we run out of memory
          // Alternative solution would be to run a cleanup periodically
          .dropEventsOlderThan(deadline)
        current + (event.eventType -> updatedCounter)
      }
    } yield ()

  private def currentDeadline(): F[Instant] =
    Clock[F].realTimeInstant.map(now => now.minus(timeWindow))

  private def countWords(event: Event): EventWordCount =
    EventWordCount(
      timestamp = Instant.ofEpochSecond(event.timestamp.secondsSinceEpoch),
      wordCount = countWordsInText(event.data),
    )

  private def countWordsInText(s: String): Int =
    s.split(raw"\s+").count(_.nonEmpty)

}

object WordCounter {

  def apply[F[_] : Async](eventSource: EventSource[F], timeWindow: FiniteDuration): Resource[F, WordCounter[F]] = for {
    state <- Resource.eval(Ref[F].of(Map.empty[EventType, WindowedCounter]))
    wordCounter = new WordCounter[F](state, java.time.Duration.ofNanos(timeWindow.toNanos))
    _ <- eventSource.events
      .evalMap(e => wordCounter.process(e))
      .compile
      .drain
      .background
  } yield wordCounter

  private case class WindowedCounter(wordCount: Long, events: Queue[EventWordCount]) {

    @tailrec
    final def dropEventsOlderThan(deadline: Instant): WindowedCounter =
      events.headOption match {
        case Some(oldestEvent) if oldestEvent.timestamp.isBefore(deadline) =>
          WindowedCounter(wordCount - oldestEvent.wordCount.toLong, events.tail).dropEventsOlderThan(deadline)
        case _ =>
          this
      }

    def add(event: EventWordCount): WindowedCounter =
      WindowedCounter(wordCount + event.wordCount.toLong, events :+ event)

  }

  private object WindowedCounter {
    def empty: WindowedCounter = WindowedCounter(0L, Queue.empty)
  }

  private case class EventWordCount(timestamp: Instant, wordCount: Int)

}
