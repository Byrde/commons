package org.byrde.commons.controllers.actions.auth.definitions

import io.igl.jwt.{ClaimField, ClaimValue}
import play.api.libs.json.{JsString, JsValue}

case class Admin(value: String) extends ClaimValue {
  override val field: ClaimField =
    Admin

  override val jsValue: JsValue =
    JsString(value)
}

object Admin extends ClaimField {
  override val name =
    "admin"

  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[String].map(apply)
}



