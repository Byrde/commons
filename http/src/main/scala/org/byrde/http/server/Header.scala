package org.byrde.http.server

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.RawHeader

case class Header(name: String, value: String) {
  def toHttpHeader: HttpHeader =
    RawHeader(name, value)
  
  def toSttpHeader: sttp.model.Header =
    sttp.model.Header(name, value)
}

object Header {
  def fromHttpHeader(header: HttpHeader): Header =
    Header(header.name(), header.value())
  
  def fromSttpHeader(header: sttp.model.Header): Header =
    Header(header.name, header.value)
}
