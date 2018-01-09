package org.byrde.commons.models.utils.auth.definition

import io.igl.jwt.{ClaimField, ClaimValue}
import play.api.libs.json.{JsString, JsValue}

/**
  * Created by martin.allaire 2016.
  */
case class Admin(value: String) extends ClaimValue {
  override val field: ClaimField = Admin
  override val jsValue: JsValue = JsString(value)
}

object Admin extends ClaimField {
  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[String].map(apply)

  override val name = "admin"
}

case class Name(value: String) extends ClaimValue {
  override val field: ClaimField = Name
  override val jsValue: JsValue = JsString(value)
}

object Name extends ClaimField {
  override def attemptApply(value: JsValue): Option[ClaimValue] =
    value.asOpt[String].map(apply)

  override val name = "name"
}


