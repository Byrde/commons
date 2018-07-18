package org.byrde.commons.models.services

import play.api.libs.json.{JsObject, Json, Writes}
import play.api.mvc.{Result, Results}

trait ServiceResponse[T] {
  implicit def writes: Writes[T]

  def msg: String

  def status: Int

  def code: Int

  def response: T

  def toResult: Result =
    Results.Status(status)(toJson)

  def toJson: JsObject = Json.obj(
    "message"  -> msg,
    "status"   -> status,
    "code"     -> code,
    "response" -> Json.toJson(response)
  )
}

object ServiceResponse {
  implicit def writes[T]: Writes[ServiceResponse[T]] =
    new Writes[ServiceResponse[T]] {
      def writes(o: ServiceResponse[T]): JsObject =
        o.toJson
    }
}
