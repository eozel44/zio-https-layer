package eozel.service

import eozel.config.AppConfig
import eozel.domain.{AppError, LoginRequest, Role, User, UserProfile}
import zio._
import zio.macros.accessible

@accessible
trait UserService {

  def login(loginRequest: LoginRequest): ZIO[Any, AppError, String]

  def signupUser(user: User, role: Role): ZIO[Any, AppError, UserProfile]

}

case class UserServiceLive(
  appConfig: AppConfig
) extends UserService {

  override def login(loginRequest: LoginRequest): ZIO[Any, AppError, String] =
    ZIO.succeed("token")

  override def signupUser(user: User, role: Role): IO[AppError, UserProfile] =
    ZIO.succeed(new UserProfile(0L, user.email, user.password, user.phone, user.firstName, user.lastName, Seq(role)))

}
object UserServiceLive {

  val layer: ZLayer[Has[AppConfig], Nothing, Has[UserService]] = ZLayer.fromService[
    AppConfig,
    UserService
  ](new UserServiceLive(_))

}
