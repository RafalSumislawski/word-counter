package io.sumislawski.wordcounter.infrastructure.httpapi

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s.Port
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder

import java.net.InetSocketAddress

object HttpServer {
  def apply[F[_] : Async](routes: HttpRoutes[F], port: Port): Resource[F, Unit] =
    BlazeServerBuilder[F]
      .bindSocketAddress(new InetSocketAddress(port.value))
      .withHttpApp(routes.orNotFound)
      .resource
      .void
}
