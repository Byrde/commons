package org.byrde.client.http.play

import play.api.libs.ws.{StandaloneWSRequest, StandaloneWSResponse}

import org.byrde.client.http.HttpClientError.{HttpExecutorError, HttpTimeoutError}
import org.byrde.client.http.{HttpClient, HttpExecutor, Response}
import org.byrde.uri.Url

import zio.ZIO
import zio.clock.Clock

import scala.util.control.NonFatal

class PlayClient extends HttpClient[PlayService with Clock, StandaloneWSRequest, StandaloneWSResponse] {
  
  override def executor: HttpExecutor.Service[PlayService with Clock, StandaloneWSRequest, StandaloneWSResponse] =
    (request: StandaloneWSRequest) => {
      for {
        env <- ZIO.environment[PlayService with Clock]
        response <-
          ZIO
            .fromFuture(_ => request.execute())
            .timeoutFail(HttpTimeoutError(incompleteResponse(request)))(env.callTimeout)
            .refineOrDie {
              case timeout: HttpTimeoutError =>
                timeout
              
              case NonFatal(ex) =>
                HttpExecutorError(incompleteResponse(request))(ex)
            }
            .provide(env)
      } yield response
    }
  
  private def incompleteResponse(request: StandaloneWSRequest): Response =
    Response(
      Url.fromString(request.url),
      request.method,
      request.headers.view.mapValues(_.mkString(", ")).toMap,
      Map.empty,
      -1,
      "N/A"
    )
  
}