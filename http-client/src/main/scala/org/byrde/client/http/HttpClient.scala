package org.byrde.client.http

import org.byrde.uri.Path

import scala.concurrent.{ExecutionContext, Future}
import scala.util.ChainingSyntax

abstract class HttpClient[R, I, A](env: R)(implicit ec: ExecutionContext) extends HttpExecutor[R, I, A] with ChainingSyntax {

  private implicit def _environment: R = env

  def get[T, TT](request: Method => Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    innerRequest(request, "GET")

  def post[T, TT](request: Method => Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    innerRequest(request, "POST")

  def put[T, TT](request: Method => Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    innerRequest(request, "PUT")

  def delete[T, TT](request: Method => Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    innerRequest(request, "DELETE")

  def patch[T, TT](request: Method => Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    innerRequest(request, "PATCH")

  def proxy[T, TT](request: Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    innerRequest(_ => request, request.method)

  def request[T, TT](body: Option[T])(
    method: Method,
    path: Path,
    headers: Headers = Map.empty,
  )(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    innerRequest(Request(path, body, headers)(_), method)

  private def innerRequest[T, TT](
    requestFn: Method => Request[T],
    method: Method,
  )(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    requestFn(method).pipe { request =>
      executor
        .execute(encoder.encode(request))
        .map(_.flatMap(decoder.decode(request)))
    }

}
