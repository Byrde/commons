package org.byrde.akka.http.rejections

import org.byrde.akka.http.scaladsl.server.directives.RejectionDirective._
import org.byrde.service.response.exceptions.ClientException

import io.circe.generic.auto._
import akka.http.scaladsl.server.{Rejection, RejectionHandler}

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

object ClientExceptionRejections extends FailFastCirceSupport {
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
          rejectRequestEntityAndComplete(serviceResponseException.toJson)
      }
      .result()
}
