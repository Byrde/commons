name := """scala-logging"""

libraryDependencies ++=
  Seq(
    "com.typesafe.scala-logging" %% "scala-logging"           % "3.9.2",
    "net.logstash.logback"       % "logstash-logback-encoder" % "6.4"
  )