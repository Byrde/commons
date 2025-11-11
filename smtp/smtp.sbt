name := """smtp"""

description := "Email client abstraction with JavaMail implementation. https://github.com/Byrde/commons/tree/main/smtp"

homepage := Some(url("https://github.com/Byrde/commons/tree/main/smtp"))

libraryDependencies ++=
  Dependencies.SmtpDependencies ++ Dependencies.TypesafeConfigDependencies
