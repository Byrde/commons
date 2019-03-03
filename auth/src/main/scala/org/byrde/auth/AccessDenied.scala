package org.byrde.auth

case class AccessDenied[T <: Permission](permission: T)
