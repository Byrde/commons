package org.byrde.akka.http.rejections
import org.byrde.service.response.exceptions.ServiceResponseException.TransientServiceResponseException

import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Rejection, RejectionHandler}

object TransientServiceResponseRejections extends PlayJsonSupport {
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
