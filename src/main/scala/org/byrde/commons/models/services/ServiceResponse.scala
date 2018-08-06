package org.byrde.commons.models.services

import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.{Result, Results}

trait ServiceResponse[T] {
  implicit def writes: Writes[T]

  def `type`: ServiceResponseType

  def msg: String

  def status: Int

  def code: Int

  def response: T

  def toResult: Result =
    Results.Status(status)(toJson)

  def toJson: JsObject = Json.obj(
    ServiceResponse.`type` -> `type`.value,
    ServiceResponse.message  -> msg,
    ServiceResponse.status   -> status,
    ServiceResponse.code     -> code,
    ServiceResponse.response -> Json.toJson(response)
  )
}

object ServiceResponse {
  val `type`: String =
    "type"

  val message: String =
    "message"

  val status: String =
    "status"

  val code: String =
    "code"

  val response: String =
    "response"

  case class TransientServiceResponse[T](override val `type`: ServiceResponseType,
                                         override val msg: String,
                                         override val status: Int,
                                         override val code: Int,
                                         override val response: T)(implicit val writes: Writes[T]) extends ServiceResponse[T]

  implicit def writes[T]: Writes[ServiceResponse[T]] =
    new Writes[ServiceResponse[T]] {
      def writes(o: ServiceResponse[T]): JsObject =
        o.toJson
    }

  implicit def reads[T](implicit format: Format[T]): Reads[TransientServiceResponse[T]] = (
    (__ \ `type`).read[String].map(ServiceResponseType.apply) and
    (__ \ message).read[String] and
    (__ \ status).read[Int] and
    (__ \ code).read[Int] and
    (__ \ response).read[T]
  ) {
    (_type: ServiceResponseType, _msg: String, _status: Int, _code: Int, _response: T) =>
      TransientServiceResponse[T](_type, _msg, _status, _code, _response)(format)
  }
}
