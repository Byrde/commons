package org.byrde.client.http.play.support

import akka.util.ByteString

import play.api.libs.ws.{BodyWritable, EmptyBody, InMemoryBody}
import play.api.mvc.AnyContent

import io.circe.{Json, Printer}

trait BodyWritableSupport {

  implicit val writeableOf_EmptyBody: BodyWritable[Unit] =
    new BodyWritable(_ => EmptyBody, "text/plain")

  implicit val writeableOf_AnyContent: BodyWritable[AnyContent] =
    new BodyWritable(_ => EmptyBody, "text/plain")

  implicit def writeableOf_Json(implicit printer: Printer = Printer.noSpaces): BodyWritable[Json] =
    BodyWritable(json => InMemoryBody(ByteString.fromString(json.printWith(printer))), "application/json")

}

object BodyWritableSupport extends BodyWritableSupport