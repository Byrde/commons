package org.byrde.client.http

import org.byrde.uri.Url

import zio.ZIO

trait HttpClient[R, I, A] extends HttpExecutor[R, I, A] {

  def get[T <: RequestLike with WithMethod[T], TT](request: T, fail: Boolean = true)(implicit encoder: RequestEncoder[R, T, I], decoder: ResponseDecoder[R, A, TT]): ZIO[R, HttpClientError, TT] =
    innerRequest(request, "GET", fail)

  def post[T <: RequestLike with WithMethod[T], TT](request: T, fail: Boolean = true)(implicit encoder: RequestEncoder[R, T, I], decoder: ResponseDecoder[R, A, TT]): ZIO[R, HttpClientError, TT] =
    innerRequest(request, "POST", fail)

  def put[T <: RequestLike with WithMethod[T], TT](request: T, fail: Boolean = true)(implicit encoder: RequestEncoder[R, T, I], decoder: ResponseDecoder[R, A, TT]): ZIO[R, HttpClientError, TT] =
    innerRequest(request, "PUT", fail)

  def delete[T <: RequestLike with WithMethod[T], TT](request: T, fail: Boolean = true)(implicit encoder: RequestEncoder[R, T, I], decoder: ResponseDecoder[R, A, TT]): ZIO[R, HttpClientError, TT] =
    innerRequest(request, "DELETE", fail)

  def patch[T <: RequestLike with WithMethod[T], TT](request: T, fail: Boolean = true)(implicit encoder: RequestEncoder[R, T, I], decoder: ResponseDecoder[R, A, TT]): ZIO[R, HttpClientError, TT] =
    innerRequest(request, "PATCH", fail)

  def proxy[T <: RequestLike with WithMethod[T], TT](request: T, fail: Boolean = true)(implicit encoder: RequestEncoder[R, T, I], decoder: ResponseDecoder[R, A, TT]): ZIO[R, HttpClientError, TT] =
    innerRequest(request, request.method, fail)

  def request[T, TT](body: T)(
    url: Url,
    method: Method,
    headers: Headers = Map.empty,
    fail: Boolean = true
  )(implicit encoder: RequestEncoder[R, Request[T], I], decoder: ResponseDecoder[R, A, TT]): ZIO[R, HttpClientError, TT] =
    innerRequest(Request(body, url, method, headers), method, fail)

  private def innerRequest[T <: RequestLike with WithMethod[T], TT](request: T, method: Method, fail: Boolean)(implicit encoder: RequestEncoder[R, T, I], decoder: ResponseDecoder[R, A, TT]): ZIO[R, HttpClientError, TT] =
    for {
      env <- ZIO.environment[R]
      response <- httpClient.execute(encoder.encode(request.withMethod(method))(env)).provide(env)
      mapping <- decoder.decode(response, fail)(request)(env)
    } yield mapping

}
