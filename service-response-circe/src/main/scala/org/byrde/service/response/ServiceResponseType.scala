package org.byrde.service.response

import io.circe.{Decoder, Encoder, HCursor, Json}

sealed trait ServiceResponseType {
  def value: String
}

object ServiceResponseType {
  implicit def encoder: Encoder[ServiceResponseType] =
    (a: ServiceResponseType) =>
      Json.fromString(a.value)

  implicit def decoder: Decoder[ServiceResponseType] =
    (c: HCursor) =>
      c.downField("type").as[String].map(fromString)

  private val success: String =
    "Success"

  private val error: String =
    "Error"

  object Success extends ServiceResponseType {
    override val value: String =
      success
  }

  object Error extends ServiceResponseType {
    override val value: String =
      error
  }

  def fromString(value: String): ServiceResponseType =
    value match {
      case x if x.equalsIgnoreCase(success) =>
        Success

      case x if x.equalsIgnoreCase(error) =>
        Error
    }
}
