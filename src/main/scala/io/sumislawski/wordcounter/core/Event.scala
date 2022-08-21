package io.sumislawski.wordcounter.core

import cats.Functor
import cats.effect.kernel.Clock
import cats.syntax.all._

case class Event(eventType: EventType, data: String, timestamp: Timestamp)

case class EventType(s: String) extends AnyVal {
  override def toString: String = s
}

case class Timestamp(secondsSinceEpoch: Long) extends AnyVal {
  override def toString: String = secondsSinceEpoch.toString
}

object Timestamp {
  def now[F[_] : Clock : Functor]: F[Timestamp] =
    Clock[F].realTimeInstant.map(instant => Timestamp(instant.getEpochSecond))
}