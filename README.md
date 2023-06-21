# ZIO & http4s

Build http server with Http4s and ZIO.

### dependencies:

```scala
val zio         = "1.0.11"
val catsInterop = "3.1.1.0"
val postgres    = "42.3.5"
val hikari      = "4.0.3"
val zioLogging  = "0.5.10"
val circe       = "0.14.1"
val circeEnum   = "1.7.0"
val pureConfig  = "0.17.0"
val http4s      = "0.23.10"
val fs2         = "3.2.7"
```

### code:

```scala
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
```
### run:

sbt run


### keywords:
zio, http4s, fs2