package org.byrde.service.response

case class UrlParsingException(msg: String) extends Throwable(s"Exception parsing url: $msg")
