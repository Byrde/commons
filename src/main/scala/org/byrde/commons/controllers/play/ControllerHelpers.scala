package org.byrde.commons.controllers.play
import org.byrde.commons.utils.exception.ModelValidationException

import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

trait ControllerHelpers {
  implicit def ec: ExecutionContext

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
}
