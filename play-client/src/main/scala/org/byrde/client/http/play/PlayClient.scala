package org.byrde.client.http.play

import play.api.libs.ws.{StandaloneWSRequest, StandaloneWSResponse}
import org.byrde.client.http.HttpClientError.HttpExecutorError
import org.byrde.client.http.{HttpClient, HttpExecutor, Response}
import org.byrde.uri.Url

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

class PlayClient(service: PlayService)(implicit ec: ExecutionContext) extends HttpClient[PlayService, StandaloneWSRequest, StandaloneWSResponse](service) {

  override def executor: HttpExecutor.Service[PlayService, StandaloneWSRequest, StandaloneWSResponse] =
    (request: StandaloneWSRequest) =>
      request
        .execute()
        .map(Right.apply)
        .recover {
          case NonFatal(ex) =>
            Left(HttpExecutorError(incompleteResponse(request))(ex))
        }

  private def incompleteResponse(request: StandaloneWSRequest): Response =
    Response(
      Url.fromString(request.url),
      request.method,
      request.headers.view.mapValues(_.mkString(", ")).toSeq,
      Seq.empty,
      -1,
      "N/A"
    )

}