package org.byrde.commons.controllers.actions.auth

import org.byrde.commons.models.http.requests.AuthenticatedRequest
import org.byrde.commons.models.services.CommonsServiceResponseDictionary.E0401
import org.byrde.commons.utils.auth.JsonWebTokenWrapper
import org.byrde.commons.utils.auth.conf.JwtConfig

import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

case class AuthorizedAction (
  jwtConfig: JwtConfig,
  _failSafe: Option[Call] = None,
  saltInstructions: Option[Request[_] => String] = {
    Some(request => request.headers.get("Client-IP").orElse(request.headers.get("X-Forwarded-For")).getOrElse(request.remoteAddress))
  })(override implicit val executionContext: ExecutionContext) extends ActionBuilder[AuthenticatedRequest] {
  lazy val failsafe: Future[Result] =
    Future.successful(_failSafe.fold(throw E0401)(Results.Redirect(_).withNewSession))

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    val tokenOpt =
      request.headers
        .get(jwtConfig.tokenName)
        .orElse(request.session.get(jwtConfig.tokenName))
        .orElse(request.cookies.get(jwtConfig.tokenName).fold(Option.empty[String]){
          cookie => Some(cookie.value)
        })

    tokenOpt.fold(failsafe){ token =>
      val configWSalt =
        saltInstructions.fold(jwtConfig){ instructions =>
          jwtConfig.copy(saltOpt = Some(instructions(request)))
        }

      JsonWebTokenWrapper(configWSalt).decode(token) match {
        case Success(jwt) =>
          block(AuthenticatedRequest(jwt)(request))
        case _ =>
          failsafe
      }
    }
  }
}

