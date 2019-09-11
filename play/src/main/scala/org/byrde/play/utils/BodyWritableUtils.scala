package org.byrde.play.utils

import io.circe.{Json, Printer}

import play.api.libs.ws.{BodyWritable, EmptyBody, InMemoryBody}
import play.api.mvc.AnyContent

import akka.util.ByteString

object BodyWritableUtils {
  implicit val writeableOf_EmptyBody: BodyWritable[Unit] =
    new BodyWritable(_ => EmptyBody, "text/plain")

  implicit val writeableOf_AnyContent: BodyWritable[AnyContent] =
    new BodyWritable(_ => EmptyBody, "text/plain")

  implicit def writeableOf_Json(implicit printer: Printer = Printer.noSpaces): BodyWritable[Json] =
    BodyWritable(json => InMemoryBody(ByteString.fromString(json.pretty(printer))), "application/json")
}
