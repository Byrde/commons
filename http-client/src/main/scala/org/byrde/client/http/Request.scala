package org.byrde.client.http

import org.byrde.uri.Path

case class Request[T](
  method: Method,
  path: Path,
  body: T = (),
  headers: Headers = Seq.empty,
)
