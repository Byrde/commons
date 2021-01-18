package org.byrde.http.server.support

import org.byrde.http.server._

import io.circe.generic.auto._

import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.{EndpointIO, EndpointOutput, statusMappingValueMatcher}

import scala.util.ChainingSyntax

trait CodeMapperSupport extends ChainingSyntax {
  self: CodeSupport =>
  
  private lazy val defaultMatcher: EndpointOutput.StatusMapping[ErrorResponse] =
    statusMappingValueMatcher(
      StatusCode.BadRequest,
      jsonBody[ErrorResponse]
        .description(s"Client exception! Error code: $errorCode")
        .example(Response.Default("Error", errorCode))
    ) {
      case err: ErrorResponse if err.code == errorCode => true
    }
  
  lazy val defaultMapper: EndpointOutput.OneOf[ErrorResponse, ErrorResponse] =
    sttp.tapir.oneOf[ErrorResponse](defaultMatcher)
  
  lazy val ackOutput: EndpointIO.Body[String, Response.Default] =
    jsonBody[Response.Default]
      .description(s"Default response! Success code: $successCode")
      .example(Response.Default("Success", successCode))
}
