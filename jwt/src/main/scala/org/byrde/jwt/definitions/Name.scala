package org.byrde.jwt.definitions

import io.igl.jwt.{ClaimField, ClaimValue}

import play.api.libs.json.{JsString, JsValue}

case class Name(value: String) extends ClaimValue {
  override val field: ClaimField =
    Name

  override val jsValue: JsValue =
    JsString(value)
}

object Name extends ClaimField {
  override val name =
    "nam"

  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[String].map(apply)
}