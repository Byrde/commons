package org.byrde.http.server.support

import org.byrde.http.server.Response

import io.circe.Printer
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Allow
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{MethodRejection, RejectionHandler}
import akka.util.ByteString

trait RejectionHandlingSupport extends RejectionSupport with CirceSupport {
  private lazy val default: RejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleAll[MethodRejection] { rejections =>
        respondWithHeader(Allow(rejections.map(_.supported))) {
          options {
            rejectRequestEntityAndComplete(StatusCodes.OK -> Response.Default("Options"))
          }
        } ~ rejectRequestEntityAndComplete(
          StatusCodes.MethodNotAllowed -> Response.Default("Method Not Allowed")
        )
      }
      .result()
  
  private lazy val cachedHandler: RejectionHandler =
    default
      .withFallback(RejectionHandler.default)
  
  lazy val rejectionHandler: RejectionHandler =
    cachedHandler
      .mapRejectionResponse {
        case res @ HttpResponse(_status, _, ent: HttpEntity.Strict, _) =>
          val status =
            _status.intValue
          
          val response =
            ent.data.utf8String
          
          def entity =
            HttpEntity(
              `application/json`,
              ByteString {
                Printer.noSpaces.printToByteBuffer(
                  Response.Default(normalizeString(response)).asJson,
                  `application/json`.charset.nioCharset()
                )
              }
            )
          
          parse(response)
            .flatMap(_.as[Response.Default])
            .map(_ => res)
            .getOrElse {
              res
                .withStatus(status)
                .withEntity(entity)
            }
        
        case res =>
          res
      }
  
//  private def registerHandlers(
//    initialHandler: RejectionHandler,
//    handlersToBeRegistered: Set[RejectionHandler]
//  ): RejectionHandler = {
//    @tailrec
//    def innerRegisterHandlers(iterator: Iterator[RejectionHandler], innerHandler: RejectionHandler): RejectionHandler =
//      if (iterator.hasNext)
//        innerRegisterHandlers(iterator, innerHandler.withFallback(iterator.next()))
//      else
//        innerHandler
//
//    innerRegisterHandlers(handlersToBeRegistered.iterator, initialHandler)
//  }
  
  private def stripLeadingAndTrailingQuotes(value: String): String = {
    var tmp =
      value
    
    if (tmp.startsWith("\""))
      tmp = tmp.substring(1, tmp.length)
    
    if (value.endsWith("\""))
      tmp = tmp.substring(0, tmp.length - 1)
    
    tmp
  }
  
  private def removeNewLine(value: String): String =
    value.replaceAll("\n", "")
  
  private def normalizeString(value: String): String =
    removeNewLine(stripLeadingAndTrailingQuotes(value))
}
