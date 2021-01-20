package org.byrde.http.server.support

import org.byrde.http.server.{ErrorResponse, JsonErrorResponse, Response}

import io.circe.Json
import io.circe.generic.auto._

import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.{EndpointIO, EndpointOutput, statusMappingValueMatcher}

trait CommonEndpointSupport {
  self: CodeSupport =>
  
  private lazy val errorMatcher: EndpointOutput.StatusMapping[ErrorResponse] =
    statusMappingValueMatcher(
      StatusCode.BadRequest,
      jsonBody[ErrorResponse]
        .description(s"Client exception! Error code: $errorCode")
        .example(Response.Default("Error", errorCode))
    ) {
      case err: ErrorResponse if err.code == errorCode => true
    }
  
  private lazy val jsonErrorMatcher: EndpointOutput.StatusMapping[JsonErrorResponse] =
    statusMappingValueMatcher(
      StatusCode.BadRequest,
      jsonBody[JsonErrorResponse]
        .description(s"Client exception! Error code: $errorCode")
        .example(JsonErrorResponse(Json.obj(), errorCode))
    ) {
      case err: JsonErrorResponse if err.code == errorCode => true
    }
  
  lazy val errorMapper: EndpointOutput.OneOf[ErrorResponse, ErrorResponse] =
    sttp.tapir.oneOf[ErrorResponse](errorMatcher)
  
  lazy val jsonErrorMapper: EndpointOutput.OneOf[JsonErrorResponse, JsonErrorResponse] =
    sttp.tapir.oneOf[JsonErrorResponse](jsonErrorMatcher)
  
  lazy val ackOutput: EndpointIO.Body[String, Response.Default] =
    jsonBody[Response.Default]
      .description(s"Default response! Success code: $successCode")
      .example(Response.Default("Success", successCode))
}
