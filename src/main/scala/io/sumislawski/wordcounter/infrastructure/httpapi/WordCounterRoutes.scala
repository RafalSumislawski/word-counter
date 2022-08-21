package io.sumislawski.wordcounter.infrastructure.httpapi

import cats.Monad
import cats.syntax.all._
import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Encoder, KeyEncoder}
import io.sumislawski.wordcounter.core.{EventType, WordCounter}
import io.sumislawski.wordcounter.infrastructure.httpapi.WordCounterRoutes._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpRoutes}

class WordCounterRoutes[F[_] : Monad](wordCounter: WordCounter[F]) extends Http4sDsl[F] {

  private implicit def jsonEncoderOf[A: Encoder]: EntityEncoder[F, A] =
    org.http4s.circe.jsonEncoderOf[F, A]

  def routes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / "wordCount" =>
      wordCounter.getWordCountByEventInTimeWindow()
        .flatMap(counts => Ok(WordCountResponse(counts)))
  }
}

object WordCounterRoutes {
  case class WordCountResponse(wordCountByEventType: Map[EventType, Long])

  implicit lazy val wordCountResponseEncoder: Encoder[WordCountResponse] = deriveEncoder

  implicit lazy val eventTypeKeyEncoder: KeyEncoder[EventType] = KeyEncoder.encodeKeyString.contramap[EventType](_.toString)
}