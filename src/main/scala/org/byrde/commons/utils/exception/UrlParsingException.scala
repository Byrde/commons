package org.byrde.commons.utils.exception

case class UrlParsingException(msg: String) extends Throwable(s"Exception parsing url: $msg")
