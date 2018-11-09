package org.byrde.jwt.definitions

import io.igl.jwt.{ClaimField, ClaimValue}

import play.api.libs.json.{JsString, JsValue}

case class Token(value: String) extends ClaimValue {
  override val field: ClaimField =
    Token

  override val jsValue: JsValue =
    JsString(value)
}

object Token extends ClaimField {
  override val name =
    "tok"

  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[String].map(apply)
}