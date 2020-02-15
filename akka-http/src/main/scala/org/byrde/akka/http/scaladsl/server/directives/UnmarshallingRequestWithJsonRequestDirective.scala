package org.byrde.akka.http.scaladsl.server.directives

import org.byrde.akka.http.rejections.JsonParsingRejections.JsonParsingRejection

import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{ContentTypeRange, HttpRequest}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives.reject
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString

import io.circe.{Decoder, jawn}

import scala.reflect.ClassTag
import scala.util.{Failure, Try}

trait UnmarshallingRequestWithJsonRequestDirective extends UnmarshallingRequestWithRequestDirective {

  private val unmarshallerContentTypes: Seq[ContentTypeRange] =
    List(`application/json`)

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .map {
        case ByteString.empty =>
          throw Unmarshaller.NoContentException

        case data =>
          jawn.parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
      }

  private def unmarshaller[T: ClassTag: Decoder]: FromEntityUnmarshaller[T] =
    jsonStringUnmarshaller
      .map { data =>
        data.as[T] match {
          case Right(value) =>
            value

          case Left(ex) =>
            throw ex
        }
      }

  def requestWithJsonEntity[T: ClassTag](errorCode: Int)(fn: HttpRequestWithEntity[T] => Route)(implicit decoder: Decoder[T]): Route = {
    val pf: PartialFunction[Try[(T, HttpRequest)], Directive1[HttpRequestWithEntity[T]]] = {
      case Failure(ex) =>
        reject(JsonParsingRejection(ex.getMessage, errorCode))
    }

    directive[T](pf.orElse(handler))(unmarshaller[T])(fn)
  }

}

object UnmarshallingRequestWithJsonRequestDirective extends UnmarshallingRequestWithJsonRequestDirective
