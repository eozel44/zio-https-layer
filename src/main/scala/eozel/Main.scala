package eozel

import eozel.config.AppConfig
import eozel.http.HttpApp
import eozel.service.{UserService, UserServiceLive}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.logging.slf4j.Slf4jLogger
import zio.logging.{LogAnnotation, Logging}
import zio.{App, ExitCode, Has, ULayer, ZEnv, ZIO, ZLayer}

object Main extends App {

  val loggingLayer: ULayer[Logging] = Slf4jLogger.make { (context, message) =>
    context.get(LogAnnotation.Cause) match {
      case Some(value) => s"$message cause:${value.prettyPrint}"
      case None        => message
    }
  }

  val appLayer: ZLayer[Any, Throwable, Clock with Blocking with Logging with Has[AppConfig] with Has[UserService]] =
    //base layers
    Clock.live >+> Blocking.live >+> loggingLayer >+> AppConfig.live >+>
      //service layer
      UserServiceLive.layer

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {

    val program = for {
      config <- ZIO.service[AppConfig]
      server <- HttpApp.createHttp4Server(config.http)
    } yield server

    program
      .provideLayer(appLayer)
      .exitCode

  }

}
