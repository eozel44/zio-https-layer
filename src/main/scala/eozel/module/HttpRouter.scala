package eozel.module.http

import cats.effect._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.server.Router

class HttpRouter[F[_] <: AnyRef: Sync] {
  protected val dsl: Http4sDsl[F] = new Http4sDsl[F] {}

  def routedHttpApp(
    userRoutes: (String, HttpRoutes[F])
  ): HttpApp[F] =
    Router(userRoutes).orNotFound

}
