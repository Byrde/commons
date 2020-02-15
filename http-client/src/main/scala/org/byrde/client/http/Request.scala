package org.byrde.client.http

import org.byrde.uri.Url

case class Request[T](body: T, url: Url, method: Method, headers: Headers = Map.empty) extends RequestLike with WithMethod[Request[T]] {
  def withMethod(method: Method): Request[T] =
    copy(method = method)
}
