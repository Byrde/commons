package org.byrde.play.helpers

import play.api.libs.json.{JsError, JsPath, JsSuccess, Reads}
import play.api.mvc.{BodyParser, ControllerComponents}

import io.circe.Decoder
import io.circe.parser._

import scala.concurrent.ExecutionContext
import scala.reflect.{ClassTag, classTag}
import scala.util.control.NoStackTrace

trait ControllerHelpers {
  implicit def ec: ExecutionContext

  private case class ModelValidationException[A: ClassTag](errors: scala.collection.Seq[(JsPath, scala.collection.Seq[play.api.libs.json.JsonValidationError])])
    extends Throwable(
      s"""
         |Error parsing: ${classTag[A].runtimeClass},
         |errors: [${formatErrors(errors)}]""".stripMargin) with NoStackTrace

  def jsonBodyParser[T: ClassTag](implicit reads: Reads[T], controllerComponents: ControllerComponents): BodyParser[T] =
    controllerComponents
      .parsers
      .tolerantJson
      .map {
        _.validate[T] match {
          case JsSuccess(validated, _) =>
            validated

          case JsError(errors) =>
            throw ModelValidationException[T](errors)
        }
      }

  def circeJsonBodyParser[T](implicit decoder: Decoder[T], controllerComponents: ControllerComponents): BodyParser[T] =
    controllerComponents
      .parsers
      .byteString
      .map(_.utf8String)
      .map { value =>
        parse(value)
          .flatMap(_.as[T])
          .fold(throw _, identity)
      }

  private def formatErrors(errors: scala.collection.Seq[(JsPath, scala.collection.Seq[play.api.libs.json.JsonValidationError])]): String =
    errors.foldLeft("") {
      case (acc, err) =>
        val error =
          s"(path: ${err._1.toString()}, errors: [${err._2.map(_.messages.mkString(" ")).mkString(", ")}])"

        if (acc.isEmpty)
          error
        else
          s"$acc, $error"
    }
}
