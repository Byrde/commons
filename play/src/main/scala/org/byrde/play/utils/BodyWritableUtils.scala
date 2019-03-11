package org.byrde.play.utils

import play.api.libs.ws.{BodyWritable, EmptyBody}

object BodyWritableUtils {
  implicit val writeableOf_EmptyBody: BodyWritable[Unit] =
    new BodyWritable(_ => EmptyBody, "text/plain")
}