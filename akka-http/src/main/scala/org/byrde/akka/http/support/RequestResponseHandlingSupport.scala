package org.byrde.akka.http.support

import java.util.UUID

import org.byrde.akka.http.logging.HttpRequestLogging
import org.byrde.akka.http.support.RequestResponseHandlingSupport.IdHeader

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Directive1, Route}

import scala.util.{Success, Try}

trait RequestResponseHandlingSupport extends ExceptionHandlingSupport {
  import org.byrde.akka.http.logging.HttpLogging._

  def RequestLogger: HttpRequestLogging

  def requestResponseHandler(route: Route): Route =
    cors {
      requestId { id =>
          handleExceptions(exceptionHandler) {
            addRequestId(id) {
              addResponseId(id) {
                extractRequest { request =>
                  val start =
                    System.currentTimeMillis

                  bagAndTag(start, request) {
                    route
                  }
                }
              }
            }
          }
      }
    }

  private def requestId: Directive1[IdHeader] =
    extractRequestContext.flatMap[Tuple1[IdHeader]] { ctx =>
      provide {
        ctx
          .request
          .headers
          .find(_.name().equalsIgnoreCase(IdHeader.name))
          .map(_.value())
          .map(IdHeader.apply)
          .getOrElse {
            IdHeader(UUID.randomUUID.toString)
          }
      }
    }

  private def addRequestId(id: IdHeader): Directive0 =
    mapRequest { request =>
      request.copy(
        headers =
        id +:
          request.headers
      )
    }

  private def addResponseId(id: IdHeader): Directive0 =
    mapResponseHeaders { headers =>
      id +:
        headers
    }

  private def bagAndTag(start: Long, request: HttpRequest): Directive0 =
    mapResponse { response =>
      RequestLogger
        .request(
          System.currentTimeMillis() - start,
          response.status.toString(),
          request
        )

      response
    }
}

object RequestResponseHandlingSupport {
  final case class IdHeader(id: String) extends ModeledCustomHeader[IdHeader] {
    override val renderInRequests =
      true

    override val renderInResponses =
      true

    override val companion: IdHeader.type =
      IdHeader

    override def value(): String =
      id
  }

  object IdHeader extends ModeledCustomHeaderCompanion[IdHeader] {
    override def name: String =
      "X-Correlation-Id"

    override def parse(value: String): Try[IdHeader] =
      Success(new IdHeader(value))
  }
}

