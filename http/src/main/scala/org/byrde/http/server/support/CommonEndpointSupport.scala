package org.byrde.http.server.support

import org.byrde.http.server.Response

import io.circe.generic.auto._

import sttp.tapir.EndpointIO
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody

trait CommonEndpointSupport {
  self: CodeSupport =>
  
  lazy val ackOutput: EndpointIO.Body[String, Response.Default] =
    jsonBody[Response.Default]
      .description(s"Default response! Success code: $successCode")
      .example(Response.Default("Success", successCode))
}
