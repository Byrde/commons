package org.byrde.http.server.support

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import org.byrde.http.server.support.RequestIdSupport.IdHeader

import java.util.UUID

import scala.util.{Success, Try}

trait RequestIdSupport {
  type RequestId = String

  implicit class HttpRequest2HttpRequestId(request: HttpRequest) {
    def requestId: RequestId =
      request
        .headers
        .find(_.name.equalsIgnoreCase(IdHeader.name))
        .map(_.value)
        .map(UUID.fromString)
        .getOrElse(UUID.randomUUID)
        .toString
  }
}

object RequestIdSupport {
  final case class IdHeader(id: String) extends ModeledCustomHeader[IdHeader] {
    override val renderInRequests: Boolean =
      true
    
    override val renderInResponses: Boolean =
      true
    
    override val companion: IdHeader.type =
      IdHeader
    
    override def value(): String =
      id
  }
  
  object IdHeader extends ModeledCustomHeaderCompanion[IdHeader] {
    override def name: String =
      "X-Request-Id"
    
    override def parse(value: String): Try[IdHeader] =
      Success(new IdHeader(value))
  }
}
