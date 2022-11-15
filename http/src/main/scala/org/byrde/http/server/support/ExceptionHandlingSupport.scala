package org.byrde.http.server.support

import org.byrde.http.server.Ack
import org.byrde.http.server.logging.HttpRequestLog
import org.byrde.logging.Logger

import java.util.UUID

import io.circe.generic.auto._
import io.circe.syntax._

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler

import scala.util.chaining._

trait ExceptionHandlingSupport extends CirceSupport {
  def exceptionHandler(logger: Logger): ExceptionHandler =
    ExceptionHandler { case ex => ctx =>
      logger.logError(s"ExceptionSupport.exceptionHandler: ${ex.getClass.getSimpleName}", HttpRequestLog(ctx.request), ex)
      ctx.complete((StatusCodes.InternalServerError, Ack(Option(ex.getMessage).getOrElse("error")).asJson))
    }
}
