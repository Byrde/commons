package org.byrde.service.response

import org.byrde.service.response.ServiceResponse.TransientServiceResponse
import org.byrde.service.response.Status.S0200

import com.github.ghik.silencer.silent

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json}

trait ServiceResponse[T] {
  def `type`: ServiceResponseType

  def status: Status

  def code: Int

  def response: T

  @silent def toJson(implicit encoder: Encoder[T]): Json =
    TransientServiceResponse(`type`, status, code, response).asJson

  def isClientError: Boolean =
    `type` == ServiceResponseType.Error && status.isClientError

  def isServerError: Boolean =
    `type` == ServiceResponseType.Error && status.isServerError
}

object ServiceResponse {
  case class TransientServiceResponse[T](override val `type`: ServiceResponseType,
                                         override val status: Status,
                                         override val code: Int,
                                         override val response: T) extends ServiceResponse[T]
  def apply[T](_response: T): TransientServiceResponse[T] =
    apply(200, _response)

  def apply[T](_code: Int, _response: T): TransientServiceResponse[T] =
    apply(ServiceResponseType.Success, _code, _response)

  def apply[T](_type: ServiceResponseType, _code: Int, _response: T): TransientServiceResponse[T] =
    apply(_type, S0200, _code, _response)

  def apply[T](_type: ServiceResponseType, _status: Status, _code: Int, _response: T): TransientServiceResponse[T] =
    TransientServiceResponse[T](_type, _status, _code, _response)
}
