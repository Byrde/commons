package org.byrde.commons.controllers

import org.byrde.commons.utils.exception.ModelValidationException

import play.api.libs.json.{JsError, JsSuccess, Reads}
import play.api.mvc.{BodyParser, ControllerComponents}

trait ControllerHelpers {
  def jsonBodyParser[T](implicit reads: Reads[T], controllerComponents: ControllerComponents): BodyParser[T] =
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
