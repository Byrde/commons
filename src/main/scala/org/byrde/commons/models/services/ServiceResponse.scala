package org.byrde.commons.models.services

import play.api.libs.json.{JsObject, Json, Writes}
import play.api.mvc.{Result, Results}

trait ServiceResponse[T] {
  implicit def writes: Writes[T]
  def msg: String
  def status: Int
  def code: Int
  def response: T

  def withMessage(_msg: String): ServiceResponse[T] = {
    val _writes   = writes
    val _code     = code
    val _status   = status
    val _response = response
    new ServiceResponse[T] {
      override implicit def writes: Writes[T] = _writes
      override def msg: String                = _msg
      override def status: Int                = _status
      override def code: Int                  = _code
      override def response: T                = _response
    }
  }

  def withCode(_code: Int): ServiceResponse[T] = {
    val _writes   = writes
    val _msg      = msg
    val _status   = status
    val _response = response
    new ServiceResponse[T] {
      override implicit def writes: Writes[T] = _writes
      override def msg: String                = _msg
      override def status: Int                = _status
      override def code: Int                  = _code
      override def response: T                = _response
    }
  }

  def withResponse[A](_response: A)(
      implicit _writes: Writes[A]): ServiceResponse[A] = {
    val _msg    = msg
    val _code   = code
    val _status = status
    new ServiceResponse[A] {
      override val writes: Writes[A] = _writes
      override val msg: String       = _msg
      override val code: Int         = _code
      override val status: Int       = _status
      override val response: A       = _response
    }
  }

  def toResult: Result = Results.Status(status)(toJson)

  def toJson: JsObject = Json.obj(
    "message"  -> msg,
    "status"   -> status,
    "code"     -> code,
    "response" -> Json.toJson(response)
  )
}

object ServiceResponse {
  implicit val writes: Writes[ServiceResponse[_]] =
    new Writes[ServiceResponse[_]] {
      def writes(o: ServiceResponse[_]): JsObject = o.toJson
    }
}
