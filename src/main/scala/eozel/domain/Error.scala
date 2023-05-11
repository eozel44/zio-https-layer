package eozel.domain

import io.circe._
import io.circe.generic.JsonCodec
import io.circe.syntax._

@JsonCodec sealed trait AppError                          extends Throwable
@JsonCodec case class AppDaoError(message: String)        extends RuntimeException(message) with AppError
@JsonCodec case class AppConfigError(message: String)     extends IllegalStateException(message) with AppError
@JsonCodec case class AlreadyExistsError(message: String) extends RuntimeException(message) with AppError
@JsonCodec case class NotFoundError(message: String)      extends RuntimeException(message) with AppError

@JsonCodec case class AppHttpError(statusCode: Int, message: String, uri: String, request: Json) extends AppError
@JsonCodec case class ResponseBody[T](value: Option[T], error: Option[AppHttpError] = None)
object ResponseBody {

  implicit val encodeLmsResponseBody: Encoder[ResponseBody[Nothing]] = new Encoder[ResponseBody[Nothing]] {
    final def apply(a: ResponseBody[Nothing]): Json = Json.obj(
      ("error", a.error.asJson),
      ("value", Json.Null)
    )
  }

  def apply(error: AppHttpError): ResponseBody[Nothing] =
    ResponseBody(None, Some(error))

  val unauthorized: ResponseBody[Nothing] =
    ResponseBody(None, Some(AppHttpError(401, "unauthorized", "", Json.Null)))
  // implicit val encodeLmsResponseBody: Encoder[LmsResponseBody] = Encoder
  // implicit val decodeLmsResponseBody: Decoder[LmsResponseBody] = Decoder[LmsResponseBody]
}

object AppError {
  implicit val decodeAppError: Decoder[AppError] = Decoder[AppDaoError]
    .map[AppError](identity)
    .or(Decoder[AppConfigError].map[AppError](identity))
    .or(Decoder[AlreadyExistsError].map[AppError](identity))
    .or(Decoder[NotFoundError].map[AppError](identity))
    .or(Decoder[AppHttpError].map[AppError](identity))

  implicit val encodeAppError: Encoder[AppError] = Encoder.instance {
    case e1 @ AppDaoError(_)           => e1.asJson
    case e2 @ AppConfigError(_)        => e2.asJson
    case e3 @ AlreadyExistsError(_)    => e3.asJson
    case e4 @ NotFoundError(_)         => e4.asJson
    case e5 @ AppHttpError(_, _, _, _) => e5.asJson
  }
}
