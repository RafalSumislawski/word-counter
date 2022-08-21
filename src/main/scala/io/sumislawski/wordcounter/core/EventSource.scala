package io.sumislawski.wordcounter.core

trait EventSource[F[_]] {
  def events: fs2.Stream[F, Event]
}
