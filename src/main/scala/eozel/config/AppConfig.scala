package eozel.config

import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio._

import scala.util.control.NoStackTrace

case class HttpConfig(host: String, port: Int, baseUrl: String)

case class AppConfig(http: HttpConfig)

object AppConfig {

  def live: ZLayer[Any, Throwable, Has[AppConfig]] =
    ZIO
      .fromEither(ConfigSource.default.load[AppConfig])
      .foldM(
        err => ZIO.fail(new IllegalArgumentException(s"config error: $err") with NoStackTrace),
        v => ZIO(v)
      )
      .toLayer
}
