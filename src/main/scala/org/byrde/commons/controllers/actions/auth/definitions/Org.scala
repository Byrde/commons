package org.byrde.commons.controllers.actions.auth.definitions

import io.igl.jwt.{ClaimField, ClaimValue}

import play.api.libs.json.{JsString, JsValue}

case class Org(value: String) extends ClaimValue {
  override val field: ClaimField =
    Org

  override val jsValue: JsValue =
    JsString(value)
}

object Org extends ClaimField {
  override val name =
    "org"

  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[String].map(apply)
}
