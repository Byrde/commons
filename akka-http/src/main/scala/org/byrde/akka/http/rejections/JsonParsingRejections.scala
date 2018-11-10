package org.byrde.akka.http.rejections

import org.byrde.akka.http.scaladsl.server.directives.RejectionDirective._
import org.byrde.service.response.CommonsServiceResponseDictionary.E0400

import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

import akka.http.scaladsl.server.{Rejection, RejectionHandler}

object JsonParsingRejections extends PlayJsonSupport {
  case class JsonParsingRejection(errors: String, errorCode: Int) extends Rejection

  case class JsonValidationRejection(errors: String, errorCode: Int) extends Rejection

  val handler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case JsonParsingRejection(errors, errorCode) =>
          rejectRequestEntityAndComplete(E0400(errors, errorCode).toJson)
      }
      .handle {
        case JsonValidationRejection(errors, errorCode) =>
          rejectRequestEntityAndComplete(E0400(errors, errorCode).toJson)
      }
      .result()
}
