import sbt._

object Dependencies {

  private object Versions {
    val zio         = "1.0.11"        
    val catsInterop = "3.1.1.0"    
    val postgres    = "42.3.5"
    val hikari      = "4.0.3"
    val zioLogging  = "0.5.10"
    val circe       = "0.14.1"
    val circeEnum   = "1.7.0"
    val pureConfig  = "0.17.0"
    val http4s      = "0.23.10"
  }

  object Libraries {

    val zio = Seq(
      "dev.zio" %% "zio"          % Versions.zio,
      "dev.zio" %% "zio-streams"  % Versions.zio,
      "dev.zio" %% "zio-macros"   % Versions.zio,
      "dev.zio" %% "zio-test"     % Versions.zio % Test,
      "dev.zio" %% "zio-test-sbt" % Versions.zio % Test
    )
       
    val catsInterop = Seq(
      "dev.zio" %% "zio-interop-cats" % Versions.catsInterop
    )
   
    val zioLogging = Seq(
      "dev.zio" %% "zio-logging"       % Versions.zioLogging,
      "dev.zio" %% "zio-logging-slf4j" % Versions.zioLogging
    )

    val circe = Seq(
      "io.circe" %% "circe-core"    % Versions.circe,
      "io.circe" %% "circe-parser"  % Versions.circe,
      "io.circe" %% "circe-generic" % Versions.circe,
      "com.beachape" %% "enumeratum-circe" % Versions.circeEnum
    )

    val pureConfig = Seq(
      "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig
    )

    val logback = Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3"
    )

    val http4s = Seq(
      "org.http4s" %% "http4s-blaze-server" % Versions.http4s,
      "org.http4s" %% "http4s-circe"        % Versions.http4s,
      "org.http4s" %% "http4s-dsl"          % Versions.http4s
    )

  }
}
