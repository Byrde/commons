package org.byrde.client.http.play

import play.api.libs.ws.{StandaloneWSRequest, StandaloneWSResponse}

import org.byrde.client.http.HttpClientError.HttpExecutorError
import org.byrde.client.http.play.support.RequestSupport
import org.byrde.client.http.{HttpClientError, HttpExecutor, RequestLike, ResponseLike}

import zio.ZIO

import scala.util.control.NonFatal

trait PlayExecutor extends HttpExecutor[PlayService, StandaloneWSRequest, StandaloneWSResponse] with RequestSupport {

  override def httpClient: HttpExecutor.Service[PlayService, StandaloneWSRequest, StandaloneWSResponse] =
    new HttpExecutor.Service[PlayService, StandaloneWSRequest, StandaloneWSResponse] {
      //TODO: Implement circuit breaker
      override def execute[T <: RequestLike](request: StandaloneWSRequest): ZIO[PlayService, HttpClientError, StandaloneWSResponse] =
        for {
          _ <- ZIO.environment[PlayService]
          response <-
            ZIO
              .fromFuture(_ => request.execute())
              .refineOrDie {
                case NonFatal(ex) =>
                  HttpExecutorError(ResponseLike(request.toRequest))(ex)
              }
        } yield response
    }

}
