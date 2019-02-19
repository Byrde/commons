package org.byrde.logging
import io.circe.Json

trait JsonLoggingFormat[-T] {
  def format(elem: T): Json

  def format(msg: String, elem: T): Json =
    Json.obj("elem" -> format(elem), "message" -> Json.fromString(msg))

  def format(msg: Json, elem: T): Json =
    Json.obj("elem" -> format(elem), "message" -> msg)
}