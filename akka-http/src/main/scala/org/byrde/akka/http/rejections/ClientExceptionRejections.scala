package org.byrde.akka.http.rejections

import org.byrde.service.response.exceptions.ClientException

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.{Rejection, RejectionHandler}

object ClientExceptionRejections {
  case class InnerRejection(ex: ClientException) extends Rejection

  implicit class ServiceResponseExceptionToRejection(ex: ClientException) {
    def toRejection: InnerRejection =
      InnerRejection(ex)
  }

  val handler: RejectionHandler =
    RejectionHandler
      .newBuilder
      .handle {
        case InnerRejection(serviceResponseException) =>
          complete(serviceResponseException)
      }
      .result()
}
