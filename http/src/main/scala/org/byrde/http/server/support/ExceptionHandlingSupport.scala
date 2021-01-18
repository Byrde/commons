package org.byrde.http.server.support

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler

import org.byrde.http.server.Response
import org.byrde.logging.Logger
import org.byrde.support.IdGenerator
import org.byrde.http.server.logging.HttpRequestLog

import io.circe.syntax._
import io.circe.generic.auto._

import scala.util.ChainingSyntax

trait ExceptionHandlingSupport extends CirceSupport with CodeSupport with ChainingSyntax {
  def logger: Logger
  
  //Use ExceptionHandler for all server errors
  lazy val exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case ex => ctx =>
        IdGenerator.generateShortId(4).pipe { id =>
          logger.error(
            s"ExceptionSupport.exceptionHandler: ${ex.getClass.getSimpleName} ($id)", ex)
          logger.error(
            s"ExceptionSupport.exceptionHandler: ${ex.getClass.getSimpleName} ($id)", HttpRequestLog(ctx.request))
          ctx.complete((StatusCodes.InternalServerError, Response.Default(ex.getMessage, errorCode).asJson))
        }
    }
}
