package org.byrde.http.server.support

import org.byrde.http.server.Response
import org.byrde.http.server.logging.HttpRequestLog
import org.byrde.logging.ScalaLogging

import java.util.UUID

import io.circe.generic.auto._
import io.circe.syntax._

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ExceptionHandler

import scala.util.ChainingSyntax

trait ExceptionHandlingSupport extends ScalaLogging with CirceSupport with ChainingSyntax {
  lazy val exceptionHandler: ExceptionHandler = ExceptionHandler { case ex => ctx =>
    generateId().pipe { id =>
      logError(s"ExceptionSupport.exceptionHandler: ${ex.getClass.getSimpleName} ($id)", ex)
      logError(s"ExceptionSupport.exceptionHandler: ${ex.getClass.getSimpleName} ($id)", HttpRequestLog(ctx.request))
      ctx.complete((StatusCodes.InternalServerError, Response.Default(Option(ex.getMessage).getOrElse("Error!")).asJson))
    }
  }
  
  private def generateId() = UUID.randomUUID.toString.take(4)
}
