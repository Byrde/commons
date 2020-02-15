package org.byrde.akka.http.scaladsl.server.directives

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.BasicDirectives.{cancelRejections, extractRequestContext, provide}
import akka.http.scaladsl.server.directives.FutureDirectives.onComplete
import akka.http.scaladsl.server.directives.RouteDirectives.reject
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}

import scala.util.{Failure, Success, Try}

trait UnmarshallingRequestWithRequestDirective {

  def requestWithEntity[T](um: FromEntityUnmarshaller[T]): Directive1[HttpRequestWithEntity[T]] =
    directive[T](handler)(um)

  protected def directive[T](pf: PartialFunction[Try[(T, HttpRequest)], Directive1[HttpRequestWithEntity[T]]])(um: FromEntityUnmarshaller[T]): Directive1[HttpRequestWithEntity[T]] =
    extractRequestContext.flatMap[Tuple1[HttpRequestWithEntity[T]]] { ctx =>
      import ctx.{executionContext, materializer}
      onComplete(um(ctx.request.entity) map (_ -> ctx.request)) flatMap pf
    } & cancelRejections(RequestEntityExpectedRejection.getClass, classOf[UnsupportedRequestContentTypeRejection])

  protected def handler[T]: PartialFunction[Try[(T, HttpRequest)], Directive1[HttpRequestWithEntity[T]]] = {
    case Success((value, req)) =>
      provide(new HttpRequestWithEntity[T](value, req))

    case Failure(RejectionError(r)) =>
      reject(r)

    case Failure(Unmarshaller.NoContentException) =>
      reject(RequestEntityExpectedRejection)

    case Failure(Unmarshaller.UnsupportedContentTypeException(x)) =>
      reject(UnsupportedRequestContentTypeRejection(x, Option.empty))

    case Failure(x: IllegalArgumentException) =>
      reject(ValidationRejection(x.getMessage, Some(x)))

    case Failure(x) =>
      reject(MalformedRequestContentRejection(x.getMessage, x))
  }

}

object UnmarshallingRequestWithRequestDirective extends UnmarshallingRequestWithRequestDirective