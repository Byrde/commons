package org.byrde.commons.controllers.actions.auth

import io.igl.jwt.Jwt

import org.byrde.commons.controllers.actions.auth.definitions.Admin
import org.byrde.commons.models.http.requests.AuthenticatedRequest
import org.byrde.commons.models.services.CommonsServiceResponseDictionary.E0401

import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class AdministrativeAction(
  _failSafe: Option[Call])(override implicit val executionContext: ExecutionContext) extends ActionFilter[AuthenticatedRequest] {
  private lazy val failsafe =
    _failSafe.fold(throw E0401)(Results.Redirect(_).withNewSession)

  def hasDataPrivileges(jwt: Jwt): Boolean = {
    jwt.getClaim[Admin] match {
      case Some(admin) =>
        Try(admin.value.toBoolean)
          .toOption
          .fold(false)(_ => true)
      case _ =>
        false
    }
  }

  def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] =
    Future.successful {
      if (hasDataPrivileges(request.jwt))
        None
      else
        Some(failsafe)
    }
}

