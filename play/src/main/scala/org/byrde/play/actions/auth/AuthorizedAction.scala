package org.byrde.play.actions.auth

import org.byrde.jwt.JsonWebToken
import org.byrde.jwt.conf.JwtConfig
import org.byrde.play.http.requests.AuthenticatedRequest
import org.byrde.service.response.CommonsServiceResponseDictionary.E0401

import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

case class AuthorizedAction(parser: BodyParsers.Default,
                            jwtConfig: JwtConfig,
                            failSafe: Request[_] => Call = {
                              _ => throw E0401
                            },
                            saltInstructions: Request[_] => String = {
                              request =>
                                request.headers
                                  .get("Client-IP")
                                  .getOrElse(request.remoteAddress)
                            })(override implicit val executionContext: ExecutionContext)
  extends ActionBuilder[AuthenticatedRequest, AnyContent] {
  override def invokeBlock[A](request: Request[A],
                              block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    lazy val redirect =
      Future.successful(Results.Redirect(failSafe(request)).withNewSession)

    val tokenOpt =
      request.headers
        .get(jwtConfig.tokenName)
        .orElse(request.session.get(jwtConfig.tokenName))
        .orElse(
          request.cookies.get(jwtConfig.tokenName).fold(Option.empty[String]) {
            cookie =>
              Some(cookie.value)
          })

    tokenOpt.fold(redirect) { token =>
      val configWSalt =
        jwtConfig.copy(saltOpt = Some(saltInstructions(request)))

      JsonWebToken(configWSalt).decode(token) match {
        case Success(jwt) =>
          block(AuthenticatedRequest(jwt)(request))

        case _ =>
          redirect
      }
    }
  }
}


