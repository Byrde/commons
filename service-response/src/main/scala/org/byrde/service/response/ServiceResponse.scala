package org.byrde.service.response

import org.byrde.service.response.ServiceResponse.TransientServiceResponse
import org.byrde.service.response.Status.S0200

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json}

trait ServiceResponse[T] {
  def `type`: ServiceResponseType

  def status: Status

  def code: Int

  def response: T

  @unchecked
  def toJson(implicit encoder: Encoder[T]): Json =
    TransientServiceResponse(`type`, status, code, response).asJson

  def isClientError: Boolean =
    `type` == ServiceResponseType.Error && status.isClientError

  def isServerError: Boolean =
    `type` == ServiceResponseType.Error && status.isServerError
}

object ServiceResponse {
  case class TransientServiceResponse[T](
    override val `type`: ServiceResponseType,
    override val status: Status,
    override val code: Int,
    override val response: T
  ) extends ServiceResponse[T]
  
  object TransientServiceResponse {
    def apply[T](_response: T): TransientServiceResponse[T] =
      apply(S0200.value, _response)
    
    def apply[T](_code: Int, _response: T): TransientServiceResponse[T] =
      apply(S0200, _code, _response)
    
    def apply[T](_status: Status, _code: Int, _response: T): TransientServiceResponse[T] =
      TransientServiceResponse(ServiceResponseType.Success, _status, _code, _response)
  }
}
