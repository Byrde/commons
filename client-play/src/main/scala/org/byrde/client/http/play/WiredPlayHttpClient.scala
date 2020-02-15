package org.byrde.client.http.play

import akka.actor.ActorSystem

import play.api.libs.ws.{StandaloneWSRequest, StandaloneWSResponse}

import org.byrde.client.http._

import com.typesafe.config.Config

import zio.IO

abstract class WiredPlayHttpClient(config: Config)(implicit system: ActorSystem) extends PlayHttpClient {

  private val client =
    PlayService(config)

  def wiredGet[T <: RequestLike with WithMethod[T], TT](
    request: T,
    fail: Boolean = true
  )(
    implicit encoder: RequestEncoder[PlayService, T, StandaloneWSRequest],
    decoder: ResponseDecoder[PlayService, StandaloneWSResponse, TT]
  ): IO[HttpClientError, TT] =
    super.get(request, fail).provide(client)

  def wiredPost[T <: RequestLike with WithMethod[T], TT](
    request: T,
    fail: Boolean = true
  )(
    implicit encoder: RequestEncoder[PlayService, T, StandaloneWSRequest],
    decoder: ResponseDecoder[PlayService, StandaloneWSResponse, TT]
  ): IO[HttpClientError, TT] =
    post(request, fail).provide(client)

  def wiredPut[T <: RequestLike with WithMethod[T], TT](
    request: T,
    fail: Boolean = true
  )(
    implicit encoder: RequestEncoder[PlayService, T, StandaloneWSRequest],
    decoder: ResponseDecoder[PlayService, StandaloneWSResponse, TT]
  ): IO[HttpClientError, TT] =
    put(request, fail).provide(client)

  def wiredDelete[T <: RequestLike with WithMethod[T], TT](
    request: T,
    fail: Boolean = true
  )(
    implicit encoder: RequestEncoder[PlayService, T, StandaloneWSRequest],
    decoder: ResponseDecoder[PlayService, StandaloneWSResponse, TT]
  ): IO[HttpClientError, TT] =
    delete(request, fail).provide(client)

  def wiredPatch[T <: RequestLike with WithMethod[T], TT](
    request: T,
    fail: Boolean = true
  )(
    implicit encoder: RequestEncoder[PlayService, T, StandaloneWSRequest],
    decoder: ResponseDecoder[PlayService, StandaloneWSResponse, TT]
  ): IO[HttpClientError, TT] =
    patch(request, fail).provide(client)

  def wiredProxy[T <: RequestLike with WithMethod[T], TT](
    request: T,
    fail: Boolean = true
  )(
    implicit encoder: RequestEncoder[PlayService, T, StandaloneWSRequest],
    decoder: ResponseDecoder[PlayService, StandaloneWSResponse, TT]
  ): IO[HttpClientError, TT] =
    proxy(request, fail).provide(client)

}
