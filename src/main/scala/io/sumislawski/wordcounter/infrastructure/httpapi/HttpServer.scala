package io.sumislawski.wordcounter.infrastructure.httpapi

import cats.effect.{Async, Resource}
import cats.syntax.all._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder

import java.net.InetSocketAddress

object HttpServer {
  def apply[F[_] : Async](routes: HttpRoutes[F], bindAddress: InetSocketAddress): Resource[F, Unit] =
    BlazeServerBuilder[F]
      .bindSocketAddress(bindAddress)
      .withHttpApp(routes.orNotFound)
      .resource
      .void
}
