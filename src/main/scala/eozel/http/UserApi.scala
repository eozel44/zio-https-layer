package eozel.http

import eozel.config._
import eozel.domain.ResponseBody._
import eozel.domain.{ResponseBody, _}
import eozel.http.HttpError
import eozel.service.UserService
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, multipart, _}
import zio._
import zio.blocking._
import zio.interop.catz._
import zio.logging.{Logging, log}
import zio.stream.interop.fs2z._
import zio.stream.{ZSink, ZStream}

import java.nio.file.FileSystems
import java.time.Instant

class UserApi[R <: Has[UserService] with Logging with Blocking](fileConfig: FileConfig) {

  type UserTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[UserTask, A] =
    jsonOf[UserTask, A]
  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A]): EntityEncoder[UserTask, A] =
    jsonEncoderOf[UserTask, A]

  val dsl: Http4sDsl[UserTask] = Http4sDsl[UserTask]
  import dsl._

  val userRoutes: HttpRoutes[UserTask] =
    HttpRoutes.of {
      case req @ POST -> Root / "login" =>
        req.decode[LoginRequest] { request =>
          UserService
            .login(request)
            .foldM(
              k => {
                val res = HttpError.errorMap(k, req.uri.path.renderString, request.asJson)
                log.error(res.asJson.toString()) *> Ok(ResponseBody(res))
              },
              t => Ok(ResponseBody(Some(t)))
            )
            .absorb
            .tapError(c =>
              log.error(HttpError.getUnexpectedError(c, req.uri.path.renderString, request.asJson).asJson.toString())
            )
            .foldM(
              t => Ok(ResponseBody(HttpError.getUnexpectedError(t, req.uri.path.renderString, request.asJson))),
              v => ZIO.succeed(v)
            )
        }

      case req @ POST -> Root / "signup" =>
        req.decode[SignupRequest] { request =>
          UserService
            .signupUser(request.asUser, Role.Admin)
            .foldM(
              k => {
                val res = HttpError.errorMap(k, req.uri.path.renderString, request.asJson)
                log.error(res.asJson.toString()) *> Ok(ResponseBody(res))
              },
              t => Ok(ResponseBody(Some(t)))
            )
            .absorb
            .tapError(c =>
              log.error(HttpError.getUnexpectedError(c, req.uri.path.renderString, request.asJson).asJson.toString())
            )
            .foldM(
              t => Ok(ResponseBody(HttpError.getUnexpectedError(t, req.uri.path.renderString, request.asJson))),
              v => ZIO.succeed(v)
            )
        }

      case req @ POST -> Root / "getDocument" =>
        req.decode[UserFile] { request =>
          UserService
            .getUserFile(
              request.userId,
              request.fileType
            )
            .foldM(
              k => {
                val res = HttpError.errorMap(k, req.uri.path.renderString, request.asJson)
                log.error(res.asJson.toString()) *> Ok(ResponseBody(res))
              },
              t => {
                val path = t.get.path
                val stream: fs2.Stream[UserTask, Byte] =
                  ZStream.fromFile(FileSystems.getDefault().getPath(path), 1024).toFs2Stream
                // val stream:fs2.Stream[ZIO[Blocking,Throwable,Byte]]
                Ok(stream)
              }
            )
            .absorb
            .tapError(c =>
              log.error(
                HttpError.getUnexpectedError(c, req.uri.path.renderString, request.asJson).asJson.toString()
              )
            )
            .foldM(
              t => Ok(ResponseBody(HttpError.getUnexpectedError(t, req.uri.path.renderString, request.asJson))),
              v => ZIO.succeed(v)
            )
        }

      case req @ POST -> Root / "saveDocument" =>
        implicitly[EntityDecoder[UserTask, multipart.Multipart[UserTask]]]
          .decode(
            req,
            strict = false
          )
          .value
          .flatMap {
            case Left(err) =>
              Ok(ResponseBody(new AppHttpError(400, err.getMessage(), req.uri.path.renderString, "".asJson)))
            case Right(mp) => {

              val part     = mp.parts.head
              val fileName = part.name.get
              val fileType = part.name.get.split('.').head
              val path     = fileConfig.rootPath.concat(fileName)
              val sink     = ZSink.fromFile(FileSystems.getDefault().getPath(path))
              val stream   = part.body.toZStream(16)

              UserService
                .createUserFile(
                  new UserFile(
                    1L,
                    1L,
                    UserFileType.values.filter(k => k.entryName.toLowerCase == fileType.toLowerCase()).head,
                    path,
                    None,
                    Instant.now
                  )
                )
                .foldM(
                  k => {
                    val res = HttpError.getUnexpectedError(k, req.uri.path.renderString, "".asJson)
                    log.error(res.asJson.toString()) *> Ok(ResponseBody(res))
                  },
                  t => stream.run(sink) *> Ok(ResponseBody(Some(t)))
                )
                .absorb
                .tapError(c =>
                  log.error(
                    HttpError.getUnexpectedError(c, req.uri.path.renderString, "".asJson).asJson.toString()
                  )
                )
                .foldM(
                  t => Ok(ResponseBody(HttpError.getUnexpectedError(t, req.uri.path.renderString, "".asJson))),
                  v => ZIO.succeed(v)
                )
            }
          }
    }
}

object UserApi {

  def apply[R <: Has[UserService] with Logging with Blocking](fileConfig: FileConfig) = new UserApi[R](fileConfig)
}
