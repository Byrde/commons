package org.byrde.akka.http.rejections

import org.byrde.service.response.exceptions.ServiceResponseException.TransientServiceResponseException

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Rejection, RejectionHandler}

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

object TransientServiceResponseRejections extends FailFastCirceSupport {
  final case class TransientServiceResponseRejection(ex: TransientServiceResponseException)
    extends Rejection

  lazy val handler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case TransientServiceResponseRejection(ex) =>
          complete(ex.toJson)
      }
      .result()
}
