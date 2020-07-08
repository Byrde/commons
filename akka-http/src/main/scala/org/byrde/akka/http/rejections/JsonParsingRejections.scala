package org.byrde.akka.http.rejections

import org.byrde.akka.http.scaladsl.server.directives.RejectionDirective._
import org.byrde.service.response.CommonsServiceResponseDictionary.E0400
import akka.http.scaladsl.server.{Rejection, RejectionHandler}

import org.byrde.akka.http.support.CirceSupport.FailFastCirceSupport

import io.circe.generic.auto._

object JsonParsingRejections extends FailFastCirceSupport {

  case class JsonParsingRejection(errors: String, errorCode: Int) extends Rejection

  val handler: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handle {
        case JsonParsingRejection(_, errorCode) =>
          rejectRequestEntityAndComplete(E0400(errorCode).toJson)
      }
      .result()

}
