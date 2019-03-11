package org.byrde.clients.utils

import play.api.libs.json._

import scala.language.implicitConversions
import scala.reflect.runtime.universe.TypeTag

object JsonUtils {
  implicit class JsObjectHelpers(val json: JsObject) extends AnyVal {
    @inline def +?(option: Option[(String, JsValue)]): JsObject =
      option.fold(json)(json + _)

    @inline def ++?(option: Option[JsObject]): JsObject =
      option.fold(json)(json ++ _)
  }

  object Reads {
    implicit def string(key: String): Reads[String] =
      (json: JsValue) => (json \ key).validate[String]
  }

  object Writes {
    implicit def string(key: String): OWrites[String] =
      (o: String) => Json.obj(key -> JsString(o))
  }

  object Format {
    def string(key: String): Format[String] =
      OFormat(Reads.string(key), Writes.string(key))
  }

  /**
    * Expensive function.
    *
    * @param json The original json to be parsed
    * @param `object` The object to return if all fields check out
    * @tparam T The type to have checked, needs to be a case class, will extract case accessors for expected fields.
    * @return
    */
  def checkUnwantedKeys[T: TypeTag](json: JsValue, `object`: T): JsResult[T] = {
    import scala.reflect.runtime.universe._

    val expectedKeys =
      typeOf[T]
        .members
        .collect {
          case method: MethodSymbol if method.isCaseAccessor =>
            method.name.toString
        }
        .toSet

    checkUnwantedKeys(json, expectedKeys, `object`)
  }


  def checkUnwantedKeys[T](json: JsValue, expectedKeys: Set[String], `object`: T): JsResult[T] = {
    val obj =
      json.asInstanceOf[JsObject]

    val keys =
      obj.keys

    val unwanted =
      keys.diff(expectedKeys)

    if (unwanted.isEmpty)
      JsSuccess(`object`)
    else
      JsError(s"Keys: ${unwanted.mkString(",")} found in the incoming JSON")
  }
}