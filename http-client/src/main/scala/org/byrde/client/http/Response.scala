package org.byrde.client.http

import org.byrde.uri.Url

case class Response(
  url: Url,
  method: Method,
  requestHeaders: Headers,
  responseHeaders: Headers,
  status: Status,
  body: Body
)