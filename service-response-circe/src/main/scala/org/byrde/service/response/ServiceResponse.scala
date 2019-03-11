package org.byrde.service.response

import org.byrde.service.response.ServiceResponse.TransientServiceResponse

import com.github.ghik.silencer.silent

import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{Encoder, Json}

trait ServiceResponse[T] {
  def `type`: ServiceResponseType

  def message: String

  def status: Int

  def code: Int

  def response: T

  @silent def toJson(implicit encoder: Encoder[T]): Json =
    TransientServiceResponse(`type`, message, status, code, response).asJson
}

object ServiceResponse {
  case class TransientServiceResponse[T](override val `type`: ServiceResponseType,
                                         override val message: String,
                                         override val status: Int,
                                         override val code: Int,
                                         override val response: T) extends ServiceResponse[T]
  def apply[T](response: T): TransientServiceResponse[T] =
    apply("Success", response)

  def apply[T](msg: String, response: T): TransientServiceResponse[T] =
    apply(msg, 200, response)

  def apply[T](msg: String, code: Int, response: T): TransientServiceResponse[T] =
    apply(ServiceResponseType.Success, msg, code, response)

  def apply[T](`type`: ServiceResponseType, msg: String, code: Int, response: T): TransientServiceResponse[T] =
    apply(`type`, msg, 200, code, response)

  def apply[T](`type`: ServiceResponseType, msg: String, status: Int, code: Int, response: T): TransientServiceResponse[T] =
    TransientServiceResponse[T](`type`, msg, status, code, response)
}
