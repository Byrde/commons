package org.byrde.http.server.support

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives.{complete, extractMaterializer, extractRequest}
import akka.http.scaladsl.server.Route

trait RejectionSupport {
  def rejectRequestEntityAndComplete(m: => ToResponseMarshallable): Route = {
    extractRequest { request =>
      extractMaterializer { implicit mat =>
        request.discardEntityBytes()
        complete(m)
      }
    }
  }
}
