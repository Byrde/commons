package org.byrde.akka.http.scaladsl.server.directives

import org.byrde.akka.http.rejections.JsonParsingRejections.JsonParsingRejection

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException

import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{ContentTypeRange, HttpRequest}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.RouteDirectives.reject
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString

import play.api.libs.json._

import scala.reflect.{ClassTag, classTag}
import scala.util.control.NoStackTrace
import scala.util.{Failure, Try}

trait UnmarshallingRequestWithJsonRequestDirective extends UnmarshallingRequestWithRequestDirective {
  private case class ModelValidationException[A: ClassTag](errors: Seq[(JsPath, Seq[play.api.libs.json.JsonValidationError])])
    extends Throwable(
      s"""
         |Error parsing: ${classTag[A].runtimeClass},
         |errors: [${formatErrors(errors)}]""".stripMargin) with NoStackTrace

  private val unmarshallerContentTypes: Seq[ContentTypeRange] =
    List(`application/json`)

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .mapWithCharset {
        case (ByteString.empty, _) =>
          throw Unmarshaller.NoContentException

        case (data, charset)       =>
          data.decodeString(charset.nioCharset.name)
      }

  private def unmarshaller[T: ClassTag: Reads]: FromEntityUnmarshaller[T] = {
    def read(json: JsValue) =
      implicitly[Reads[T]]
        .reads(json)

    jsonStringUnmarshaller
      .map { data =>
        read(Json.parse(data)) match {
          case JsSuccess(value, _) =>
            value

          case JsError(errors) =>
            throw ModelValidationException[T](errors)
        }
      }
  }

  private def formatErrors(errors: Seq[(JsPath, Seq[play.api.libs.json.JsonValidationError])]): String =
    errors.foldLeft("") {
      case (acc, err) =>
        val error =
          s"(path: ${err._1.toString()}, errors: [${err._2.map(_.messages.mkString(" ")).mkString(", ")}])"

        if (acc.isEmpty)
          error
        else
          s"$acc, $error"
    }

  def requestWithJsonEntity[T: ClassTag](errorCode: Int)(fn: HttpRequestWithEntity[T] => Route)(implicit reads: Reads[T]): Route = {
    val pf: PartialFunction[Try[(T, HttpRequest)],
                            Directive1[HttpRequestWithEntity[T]]] = {
      case Failure(ex: JsonParseException) =>
        reject(JsonParsingRejection(ex.toString, errorCode))

      case Failure(ex: JsonMappingException) =>
        reject(JsonParsingRejection(ex.toString, errorCode))

      case Failure(ex: ModelValidationException[_]) =>
        reject(JsonParsingRejection(ex.getMessage, errorCode))
    }

    directive[T](pf.orElse(handler))(unmarshaller[T])(fn)
  }
}

object UnmarshallingRequestWithJsonRequestDirective extends UnmarshallingRequestWithJsonRequestDirective