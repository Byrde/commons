package org.byrde.commons.utils.exception

import play.api.data.validation.ValidationError
import play.api.libs.json.JsPath

import scala.reflect.ClassTag
import scala.reflect._

case class ModelValidationException[A: ClassTag](
    errors: Seq[(JsPath, Seq[play.api.libs.json.JsonValidationError])])
    extends Throwable(
      s"""
       |Error parsing: ${classTag[A].runtimeClass},
       |errors: [${errors.foldLeft("") { (acc, err) =>
           (if (acc.isEmpty) acc else s"$acc ,") +
             s"(path: ${err._1.toString()}, errors: [${err._2.map(_.messages.mkString(" ")).mkString(", ")}])"
         }}]""".stripMargin)
