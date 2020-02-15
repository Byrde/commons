package org.byrde.client.http

import org.byrde.uri.Url

trait RequestLike {

  def url: Url

  def method: Method

  def headers: Headers

}
