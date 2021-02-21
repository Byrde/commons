package org.byrde.http.server.support

import io.circe.Encoder
import io.circe.syntax._

import sttp.client3.{BodySerializer, StringBody}
import sttp.model.MediaType

import scala.util.ChainingSyntax

trait BodySerializerSupport extends ChainingSyntax {
  implicit def protoBodySerializer[T](implicit encoder: Encoder[T]): BodySerializer[T] =
    _.asJson.noSpaces.pipe(StringBody.apply(_, "UTF-8", MediaType.ApplicationJson))
}
