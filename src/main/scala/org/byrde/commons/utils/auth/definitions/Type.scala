package org.byrde.commons.utils.auth.definitions

import io.igl.jwt.{ClaimField, ClaimValue}

import play.api.libs.json.{JsString, JsValue}

case class Type(value: String) extends ClaimValue {
  override val field: ClaimField =
    Type

  override val jsValue: JsValue =
    JsString(value)
}

object Type extends ClaimField {
  override val name =
    "typ"

  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[String].map(apply)
}
