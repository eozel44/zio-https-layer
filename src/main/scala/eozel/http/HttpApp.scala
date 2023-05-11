package eozel.http

import cats.effect.{ExitCode => CatsExitCode}
import eozel.config.{AppConfig, HttpConfig}
import eozel.http.UserApi
import eozel.module.http._
import eozel.service._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server
import org.http4s.server.middleware.CORS
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.interop.catz._
import zio.logging.Logging

object HttpApp {

  type Http4Server     = Has[Server]
  type AppDependencies = Clock with Blocking with Logging with Has[AppConfig] with Has[UserService]

  type AppTask[A] = RIO[AppDependencies, A]

  class ZioApp(httpConfig: HttpConfig)(implicit runtime: zio.Runtime[AppDependencies]) extends HttpRouter[AppTask]

  def createHttp4Server(
    httpConfig: HttpConfig
  ): ZIO[AppDependencies, Throwable, Unit] =
    ZIO.runtime[AppDependencies].flatMap { implicit runtime =>
      val zioApp = new ZioApp(httpConfig)
      val httpRoutes = zioApp.routedHttpApp(
        "/user" -> UserApi[AppDependencies].userRoutes
      )

      val httpApp = CORS.policy.withAllowOriginAll
        .withAllowCredentials(true)
        .withAllowHeadersAll
        .apply(httpRoutes)

      BlazeServerBuilder[AppTask]
        .withExecutionContext(runtime.platform.executor.asEC)
        .bindHttp(httpConfig.port, httpConfig.host)
        .withMaxHeadersLength(2 * 1024 * 1024)
        .withHttpApp(httpApp)
        .serve
        .compile[AppTask, AppTask, CatsExitCode]
        .drain
    }
}
