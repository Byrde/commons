package org.byrde.smtp

sealed trait SmtpConnectionType

object SmtpConnectionType {
  case object SSL extends SmtpConnectionType
  
  case object TLS extends SmtpConnectionType
  
  case object Unsecured extends SmtpConnectionType
  
  def fromStringUnsafe: String => SmtpConnectionType = {
    case x if x.equalsIgnoreCase("ssl") =>
      SSL

    case x if x.equalsIgnoreCase("tls") =>
      TLS

    case x if x.equalsIgnoreCase("unsecured") =>
      Unsecured
  }
}
