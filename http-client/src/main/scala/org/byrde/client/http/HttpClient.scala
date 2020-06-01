package org.byrde.client.http

import org.byrde.uri.Path

import zio.ZIO

trait HttpClient[R, I, A] extends HttpExecutor[R, I, A] {

  def get[T, TT](request: Method => Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): ZIO[R, HttpClientError, TT] =
    innerRequest(request, "GET")

  def post[T, TT](request: Method => Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): ZIO[R, HttpClientError, TT] =
    innerRequest(request, "POST")

  def put[T, TT](request: Method => Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): ZIO[R, HttpClientError, TT] =
    innerRequest(request, "PUT")

  def delete[T, TT](request: Method => Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): ZIO[R, HttpClientError, TT] =
    innerRequest(request, "DELETE")

  def patch[T, TT](request: Method => Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): ZIO[R, HttpClientError, TT] =
    innerRequest(request, "PATCH")

  def proxy[T, TT](request: Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): ZIO[R, HttpClientError, TT] =
    innerRequest(_ => request, request.method)

  def request[T, TT](body: Option[T])(
    method: Method,
    path: Path,
    headers: Headers = Map.empty,
  )(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): ZIO[R, HttpClientError, TT] =
    innerRequest(Request(path, body, headers)(_), method)

  private def innerRequest[T, TT](
    requestFn: Method => Request[T],
    method: Method,
  )(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): ZIO[R, HttpClientError, TT] =
    for {
      env <- ZIO.environment[R]
      request = requestFn(method)
      response <- executor.execute(encoder.encode(request)(env)).provide(env)
      mapping <- decoder.decode(response)(request)(env)
    } yield mapping

}
