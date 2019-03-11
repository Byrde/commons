package org.byrde.akka.http.rejections

import org.byrde.akka.http.scaladsl.server.directives.RejectionDirective._
import org.byrde.service.response.CommonsServiceResponseDictionary.E0400

import akka.http.scaladsl.server.{Rejection, RejectionHandler}

import io.circe.generic.auto._

import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

object JsonParsingRejections extends FailFastCirceSupport {
  case class JsonParsingRejection(errors: String, errorCode: Int) extends Rejection

  val handler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case JsonParsingRejection(errors, errorCode) =>
          rejectRequestEntityAndComplete(E0400(errors, errorCode).toJson)
      }
      .result()
}
