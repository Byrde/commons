package org.byrde.akka.http.support

import akka.http.scaladsl.model.HttpRequest

import org.byrde.akka.http.support.RequestResponseHandlingSupport.IdHeader

import java.util.UUID

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
