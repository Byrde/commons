package org.byrde.play.utils

import play.api.libs.ws.{BodyWritable, EmptyBody}
import play.api.mvc.AnyContent

object BodyWritableUtils {
  implicit val writeableOf_EmptyBody: BodyWritable[Unit] =
    new BodyWritable(_ => EmptyBody, "text/plain")

  implicit val writeableOf_AnyContent: BodyWritable[AnyContent] =
    new BodyWritable(_ => EmptyBody, "text/plain")
}