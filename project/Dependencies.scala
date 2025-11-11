import sbt.librarymanagement.{ DependencyBuilders, ModuleID }

object Dependencies extends DependencyBuilders {
  val SmtpDependencies: Seq[ModuleID] =
    Seq(
      "javax" % "javaee-api" % "8.0.1",
      "javax.mail" % "mail" % "1.4.7",
      "org.jsoup" % "jsoup" % "1.21.2",
    )

  val JwtDependencies: Seq[ModuleID] = Seq("com.github.jwt-scala" %% "jwt-circe" % "11.0.3")

  val RedisDependencies: Seq[ModuleID] = Seq("redis.clients" % "jedis" % "7.0.0")

  val TypesafeConfigDependencies: Seq[ModuleID] = Seq("com.typesafe" % "config" % "1.4.2")

  val CirceDependencies: Seq[ModuleID] =
    Seq(
      "io.circe" %% "circe-core" % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15",
      "io.circe" %% "circe-parser" % "0.14.15",
    )

  val ScalaTest: Seq[ModuleID] =
    Seq(
      "org.scalatest" %% "scalatest" % "3.2.19" % "test",
      "org.scalacheck" %% "scalacheck" % "1.18.1" % "test",
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.18.0" % "test",
    )

  val CommonsDependencies: Seq[ModuleID] = Seq("org.apache.commons" % "commons-lang3" % "3.19.0")

  val ScalaLoggingDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "net.logstash.logback" % "logstash-logback-encoder" % "9.0",
    )

  val GooglePubSubDependencies: Seq[ModuleID] =
    Seq(
      "com.google.cloud" % "google-cloud-pubsub" % "1.143.0",
    )
}
