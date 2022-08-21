package io.sumislawski.wordcounter.core

case class Event(eventType: EventType, data: String, timestamp: Timestamp)

case class EventType(s: String) extends AnyVal {
  override def toString: String = s
}

case class Timestamp(secondsSinceEpoch: Long) extends AnyVal {
  override def toString: String = secondsSinceEpoch.toString
}