name := """smtp"""

description := "Email client abstraction with JavaMail implementation."

homepage := Some(url("https://github.com/Byrde/commons/tree/main/smtp"))

libraryDependencies ++=
  Dependencies.SmtpDependencies ++ Dependencies.TypesafeConfigDependencies
