package org.byrde.commons.models.http.requests

import io.igl.jwt.Jwt

import play.api.mvc.{Request, WrappedRequest}

case class AuthenticatedRequest[A](jwt: Jwt)(implicit request: Request[A]) extends WrappedRequest[A](request)
