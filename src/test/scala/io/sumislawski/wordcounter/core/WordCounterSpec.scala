package io.sumislawski.wordcounter.core

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.testkit.TestControl
import cats.effect.{IO, Resource}
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.{DurationInt, DurationLong, FiniteDuration}

class WordCounterSpec extends AsyncFunSuite with AsyncIOSpec with Matchers {

  test("Counting words in one event") {
    TestControl.executeEmbed(
      wordCounterWithMockedEventSource(timeWindow = 5.seconds,
        Event(EventType("type1"), " word1, word2\tword3; this-is-one-word4\nword5    word6\r\n", Timestamp(0L)),
      ).use { wordCounter =>
        IO.sleep(1.seconds) >>
          wordCounter.getWordCountByEventInTimeWindow()
            .asserting(_ shouldEqual Map(EventType("type1") -> 6L))
      }
    )
  }

  test("Counting multiple events of one type") {
    TestControl.executeEmbed(
      wordCounterWithMockedEventSource(timeWindow = 5.seconds,
        Event(EventType("type1"), "word1 word2", Timestamp(0L)),
        Event(EventType("type1"), "word1", Timestamp(0L)),
      ).use { wordCounter =>
        IO.sleep(1.seconds) >>
          wordCounter.getWordCountByEventInTimeWindow()
            .asserting(_ shouldEqual Map(EventType("type1") -> 3L))
      }
    )
  }

  test("Counting separately for different types of events") {
    TestControl.executeEmbed(
      wordCounterWithMockedEventSource(timeWindow = 5.seconds,
        Event(EventType("type1"), "word1 word2", Timestamp(0L)),
        Event(EventType("type2"), "word1", Timestamp(0L)),
        Event(EventType("type1"), "word1", Timestamp(0L)),
        Event(EventType("type3"), "word1 word2 word3 word4", Timestamp(0L)),
      ).use { wordCounter =>
        IO.sleep(1.seconds) >>
          wordCounter.getWordCountByEventInTimeWindow()
            .asserting(_ shouldEqual Map(
              EventType("type1") -> 3L,
              EventType("type2") -> 1L,
              EventType("type3") -> 4L,
            ))
      }
    )
  }

  test("Not counting events older than the timeWindow") {
    TestControl.executeEmbed(
      wordCounterWithMockedEventSource(timeWindow = 5.seconds,
        Event(EventType("type1"), "word1 word2 word3", Timestamp(0L)),
        Event(EventType("type1"), "word1", Timestamp(3L)),
        Event(EventType("type1"), "word1", Timestamp(6L)),
      ).use { wordCounter =>
        IO.sleep(7.seconds) >>
          wordCounter.getWordCountByEventInTimeWindow()
            .asserting(_ shouldEqual Map(EventType("type1") -> 2L))
      }
    )
  }

  private def wordCounterWithMockedEventSource(timeWindow: FiniteDuration, events: Event*): Resource[IO, WordCounter[IO]] = for {
    eventSource <- eventSourceStub(events: _*)
    wordCounter <- WordCounter[IO](eventSource, timeWindow)
  } yield wordCounter

  private def eventSourceStub(plannedEvents: Event*): Resource[IO, EventSource[IO]] = {
    Resource.pure( // The only reason why we package it in resource is to make it more convenient to use in the tests, where we need to flatMap it with WordCounter which is a Resource.
      new EventSource[IO] {
        override def events: fs2.Stream[IO, Event] = {
          fs2.Stream.emits[IO, Event](plannedEvents)
            .mapAccumulate(0L) { (lastTimestamp: Long, event: Event) =>
              val delayBetweenEvents = (event.timestamp.secondsSinceEpoch - lastTimestamp).seconds
              (
                event.timestamp.secondsSinceEpoch,
                fs2.Stream.sleep[IO](delayBetweenEvents) >> fs2.Stream.emit(event)
              )
            }.flatMap { case (_, s) => s }
        }
      }
    )
  }

}
