package eozel

import eozel.domain._
import io.circe.Json
import org.http4s.QueryParamDecoder
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

package object http {

  object HttpPagination {
    /* Necessary for decoding query parameters */
    import QueryParamDecoder._

    /* Parses out the optional offset and page size params */
    object OptionalPageSizeMatcher extends OptionalQueryParamDecoderMatcher[Int]("pageSize")
    object OptionalOffsetMatcher   extends OptionalQueryParamDecoderMatcher[Int]("offset")
  }

  object HttpError {

    def errorMap(error: AppError, uri: String, request: Json): AppHttpError = error match {
      case k: AlreadyExistsError => AppHttpError(409, k.message, uri, request)
      case k: NotFoundError      => AppHttpError(404, k.message, uri, request)
      case k: AppDaoError        => AppHttpError(422, k.message, uri, request)
      case _                     => AppHttpError(500, error.getMessage(), uri, request)
    }

    def getUnexpectedError(error: Throwable, uri: String, request: Json): AppHttpError =
      AppHttpError(500, error.getMessage(), uri, request)

  }

//  object HttpUser {
//
//    def extractRequestUserId(profiles: List[CommonProfile]): Long =
//      profiles.head.getId().toLong
//
//  }

  //({type L[A] = Either[Int, A]})#L

  // implicit class  HttpErrorSyntax[A,R,E<:Throwable](val response:ZIO[R,E,A]){

  //   def handleError(implicit dsl: Http4sDsl[({type L[X] = ZIO[R,E, X]})#L], F: Applicative[({type L[X] = ZIO[R,E, X]})#L], w: EntityEncoder[({type L[X] = ZIO[R,E, X]})#L, A]) = {
  //     import dsl._
  //     response
  //             .foldM(k => BadRequest.apply(k), Ok(_))
  //             .absorb
  //             .tapError(c => log.error(s"${c.getMessage()}"))
  //             .foldM(t => InternalServerError(LmsAppInternalServerError("unexpected error")), v => ZIO.succeed(v))

  //   }
  // }

}
