package org.byrde.http.server

import io.circe.Json

import sttp.tapir.Schema
import sttp.tapir.SchemaType.SProduct

case class JsonErrorResponse(response: Json, message: String) extends Response

object JsonErrorResponse {
  implicit lazy val schema: Schema[JsonErrorResponse] =
    Schema(SProduct.empty)
}