package org.byrde.utils

import io.circe.Printer

import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.mvc.Codec

object CirceWritableUtils {
  private implicit lazy val LocalPrinter: Printer =
    Printer.noSpaces.copy(dropNullValues = true)

  implicit val contentTypeOf_CirceJson: ContentTypeOf[io.circe.Json] =
    ContentTypeOf[io.circe.Json](Some(ContentTypes.JSON))

  implicit def writeableOf_CirceJson(implicit codec: Codec): Writeable[io.circe.Json] =
    Writeable(obj => codec.encode(LocalPrinter.pretty(obj)))
}