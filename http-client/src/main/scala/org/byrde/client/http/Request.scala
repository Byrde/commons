package org.byrde.client.http

import org.byrde.uri.Path

case class Request[T](
  path: Path,
  body: Option[T] = Option.empty,
  headers: Headers = Map.empty,
)(val method: Method)
