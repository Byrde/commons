package org.byrde.jwt.claims

import io.circe.{Encoder, Printer}
import io.circe.syntax._

import pdi.jwt.JwtClaim

case class Claims[T](
  issuer: Option[String] = Option.empty,
  subject: Option[String] = Option.empty,
  audience: Option[Set[String]] = Option.empty,
  expiration: Option[Long] = Option.empty,
  notBefore: Option[Long] = Option.empty,
  issuedAt: Option[Long] = Option.empty,
  jwtId: Option[String] = Option.empty,
  claim: Option[T] = Option.empty[T]
)(implicit encoder: Encoder[T]) {
  private lazy val printer: Printer =
    Printer.noSpaces.copy(dropNullValues = true)

  def toJwtClaim: JwtClaim =
    new JwtClaim(
      claim.map(_.asJson).map(printer.print).getOrElse("{}"),
      issuer,
      subject,
      audience,
      expiration,
      notBefore,
      issuedAt,
      jwtId,
    )
}
