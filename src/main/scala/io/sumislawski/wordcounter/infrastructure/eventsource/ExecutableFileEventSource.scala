package io.sumislawski.wordcounter.infrastructure.eventsource

import cats.effect.Sync
import io.sumislawski.wordcounter.core.{Event, EventSource}

class ExecutableFileEventSource[F[_] : Sync] extends EventSource[F] {
  override def events: fs2.Stream[F, Event] = fs2.Stream.empty
}
