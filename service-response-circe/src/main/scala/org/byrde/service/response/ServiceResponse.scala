package org.byrde.service.response

import org.byrde.service.response.ServiceResponse.TransientServiceResponse

import com.github.ghik.silencer.silent

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json}

trait ServiceResponse[T] {
  def `type`: ServiceResponseType

  def status: Int

  def code: Int

  def response: T

  @silent def toJson(implicit encoder: Encoder[T]): Json =
    TransientServiceResponse(`type`, status, code, response).asJson

  def isClientError: Boolean =
    `type` == ServiceResponseType.Error && status <= 500 && status >= 400

  def isServerError: Boolean =
    `type` == ServiceResponseType.Error && status >= 500
}

object ServiceResponse {
  case class TransientServiceResponse[T](override val `type`: ServiceResponseType,
                                         override val status: Int,
                                         override val code: Int,
                                         override val response: T) extends ServiceResponse[T]
  def apply[T](response: T): TransientServiceResponse[T] =
    apply(200, response)

  def apply[T](code: Int, response: T): TransientServiceResponse[T] =
    apply(ServiceResponseType.Success, code, response)

  def apply[T](`type`: ServiceResponseType, code: Int, response: T): TransientServiceResponse[T] =
    apply(`type`, 200, code, response)

  def apply[T](`type`: ServiceResponseType, status: Int, code: Int, response: T): TransientServiceResponse[T] =
    TransientServiceResponse[T](`type`, status, code, response)
}
