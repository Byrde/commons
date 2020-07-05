package org.byrde.client.http

import org.byrde.uri.Path

import scala.concurrent.{ExecutionContext, Future}
import scala.util.ChainingSyntax

abstract class HttpClient[R, I, A](env: R)(implicit ec: ExecutionContext) extends HttpExecutor[R, I, A] with ChainingSyntax {

  private implicit def _environment: R = env

  def get[T](
    path: Path,
    headers: Headers = Seq.empty,
  )(
    implicit encoder: RequestEncoder[R, Request[Unit], I],
    decoder: ResponseDecoder[R, I, A, T]
  ): Future[Either[HttpClientError, T]] =
    request(Request("GET", path, headers =  headers))

  def post[T, TT](body: T)(
    path: Path,
    headers: Headers = Seq.empty,
  )(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, I, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    request(Request("POST", path, body, headers))

  def put[T, TT](body: T)(
    path: Path,
    headers: Headers = Seq.empty,
  )(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, I, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    request(Request("PUT", path, body, headers))

  def delete[T, TT](body: T)(
    path: Path,
    headers: Headers = Seq.empty,
  )(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, I, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    request(Request("DELETE", path, body, headers))

  def patch[T, TT](body: T)(
    path: Path,
    headers: Headers = Seq.empty,
  )(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, I, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    request(Request("PATCH", path, body, headers))

  def request[T, TT](body: T)(
    method: Method,
    path: Path,
    headers: Headers = Seq.empty,
  )(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, I, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    request(Request(method, path, body, headers))

  def request[T, TT](request: Request[T])(
    implicit encoder: RequestEncoder[R, Request[T], I],
    decoder: ResponseDecoder[R, I, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    _request(encoder.encode(request))

  def proxy[T, TT](request: T)(
    implicit encoder: RequestEncoder[R, T, I],
    decoder: ResponseDecoder[R, I, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    _request(encoder.encode(request))

  private def _request[T, TT](request: I)(
    implicit decoder: ResponseDecoder[R, I, A, TT]
  ): Future[Either[HttpClientError, TT]] =
    executor
      .execute(request)
      .map(_.flatMap(decoder.decode(request)))

}
