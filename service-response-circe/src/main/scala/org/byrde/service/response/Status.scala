package org.byrde.service.response

import io.circe.{Decoder, Encoder}

import scala.language.implicitConversions

class Status(val value: Int) {
  def isClientError: Boolean =
    value <= 500 && value >= 400

  def isServerError: Boolean =
    value >= 500
}

object Status {
  private val statuses =
    Seq(
      S0200,
      S0400,
      S0401,
      S0403,
      S0404,
      S0405,
      S0409,
      S0415,
      S0500,
      S0502,
      S0504
    )

  implicit val encoder: Encoder[Status] =
    Encoder[Int].contramap(_.value)

  implicit val decoder: Decoder[Status] =
    Decoder.decodeInt.map(fromInt)

  object S0200 extends Status(200)

  // Client errors (1 - 499)
  object S0400 extends Status(400)
  object S0401 extends Status(401)
  object S0403 extends Status(403)
  object S0404 extends Status(404)
  object S0405 extends Status(405)
  object S0409 extends Status(409)
  object S0415 extends Status(415)

  // Server errors (500 - 999)
  object S0500 extends Status(500)
  object S0502 extends Status(502)
  object S0504 extends Status(504)

  def fromInt(_status: Int): Status =
    statuses.find(_.value == _status).get
}
