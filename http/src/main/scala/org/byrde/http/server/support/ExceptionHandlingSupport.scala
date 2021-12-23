package org.byrde.http.server.support

import org.byrde.http.server.Ack
import org.byrde.http.server.logging.HttpRequestLog
import org.byrde.logging.Logger

import java.util.UUID

import io.circe.generic.auto._
import io.circe.syntax._

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler

import scala.util.ChainingSyntax

trait ExceptionHandlingSupport extends CirceSupport with ChainingSyntax {
  def exceptionHandler(logger: Logger): ExceptionHandler =
    ExceptionHandler { case ex => ctx =>
      generateId().pipe { id =>
        logger.logError(s"ExceptionSupport.exceptionHandler: ${ex.getClass.getSimpleName} ($id)", ex)
        logger.logError(s"ExceptionSupport.exceptionHandler: ${ex.getClass.getSimpleName} ($id)", HttpRequestLog(ctx.request))
        ctx.complete((StatusCodes.InternalServerError, Ack(Option(ex.getMessage).getOrElse("error")).asJson))
      }
    }
  
  private def generateId() = UUID.randomUUID.toString.take(4)
}
