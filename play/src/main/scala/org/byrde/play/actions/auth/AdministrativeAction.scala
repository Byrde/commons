package org.byrde.play.actions.auth

import org.byrde.jwt.definitions.Admin
import org.byrde.play.http.requests.AuthenticatedRequest
import org.byrde.service.response.CommonsServiceResponseDictionary.E0401

import io.igl.jwt.Jwt

import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class AdministrativeAction(_failSafe: Option[Call])(override implicit val executionContext: ExecutionContext) extends ActionFilter[AuthenticatedRequest] {
  private lazy val failsafe =
    _failSafe.fold(throw E0401)(Results.Redirect(_).withNewSession)

  def filter[A](request: AuthenticatedRequest[A]): Future[Option[Result]] =
    Future.successful {
      if (hasAdminPrivileges(request.jwt))
        None
      else
        Some(failsafe)
    }

  private def hasAdminPrivileges(jwt: Jwt): Boolean =
    jwt.getClaim[Admin] match {
      case Some(admin) =>
        Try(admin.value.toBoolean)
          .toOption
          .fold(false)(_ => true)

      case _ =>
        false
    }
}
