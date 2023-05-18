package eozel.service

import eozel.config.AppConfig
import eozel.domain._
import zio._
import zio.macros.accessible

import java.time.Instant

@accessible
trait UserService {

  def login(loginRequest: LoginRequest): ZIO[Any, AppError, String]

  def signupUser(user: User, role: Role): ZIO[Any, AppError, UserProfile]

  def createUserFile(userFile: UserFile): IO[AppError, UserFile]

  def getUserFile(userId: Long, fileType: UserFileType): IO[AppError, Option[UserFile]]

}

case class UserServiceLive(
  appConfig: AppConfig
) extends UserService {

  override def login(loginRequest: LoginRequest): ZIO[Any, AppError, String] =
    ZIO.succeed("token")

  override def signupUser(user: User, role: Role): IO[AppError, UserProfile] =
    ZIO.succeed(new UserProfile(0L, user.email, user.password, user.phone, user.firstName, user.lastName, Seq(role)))

  override def createUserFile(userFile: UserFile): IO[AppError, UserFile] = for {
    file <- //check file exist
      ZIO.succeed(Option(None))
    crFile <- file match {
                case Some(item) => ZIO.fail(AlreadyExistsError(s"file already exists"))
                case _ =>
                  ZIO.succeed(new UserFile(1L, 1L, UserFileType.Photo, s"C:\\documents\\photo.png", None, Instant.now))
              }
  } yield crFile

  override def getUserFile(userId: Long, fileType: UserFileType): IO[AppError, Option[UserFile]] =
    ZIO.succeed(Option(new UserFile(1L, 1L, UserFileType.Photo, s"C:\\documents\\photo.png", None, Instant.now)))

}
object UserServiceLive {

  val layer: ZLayer[Has[AppConfig], Nothing, Has[UserService]] = ZLayer.fromService[
    AppConfig,
    UserService
  ](new UserServiceLive(_))

}
