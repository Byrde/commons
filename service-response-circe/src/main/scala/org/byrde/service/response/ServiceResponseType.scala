package org.byrde.service.response

import io.circe.{Decoder, Encoder, Json}

sealed trait ServiceResponseType {
  def value: String
}

object ServiceResponseType {
  implicit def encoder: Encoder[ServiceResponseType] =
    (`type`: ServiceResponseType) =>
      Json.fromString(`type`.value)

  implicit def decoder: Decoder[ServiceResponseType] =
    Decoder.decodeString.map(`type` => fromString(`type`))

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
