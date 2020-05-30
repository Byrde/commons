package org.byrde.akka.http.scaladsl.server.directives

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives.{complete, extractMaterializer, extractRequest}
import akka.http.scaladsl.server.Route

object RejectionDirective {
  
  def rejectRequestEntityAndComplete(m: => ToResponseMarshallable): Route = {
    extractRequest { request =>
      extractMaterializer { implicit mat =>
        request.discardEntityBytes()
        complete(m)
      }
    }
  }
  
}
