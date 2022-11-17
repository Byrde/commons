import sbt.librarymanagement.{DependencyBuilders, ModuleID}

object Dependencies extends DependencyBuilders {
  //TODO: No more akka
  //DO NOT TOUCH - These are the absolute last non-business licence versions of akka
  val AkkaDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.akka"  %% "akka-actor"          % "2.6.20",
      "com.typesafe.akka"  %% "akka-slf4j"          % "2.6.20",
      "com.typesafe.akka"  %% "akka-stream"         % "2.6.20",
      "com.typesafe.akka"  %% "akka-testkit"        % "2.6.20",
      "com.typesafe.akka"  %% "akka-stream-testkit" % "2.6.20",
      "com.typesafe.akka"  %% "akka-http"           % "10.2.10" exclude("com.typesafe.akka", "akka-streams_2.13"),
      "com.typesafe.akka"  %% "akka-http-testkit"   % "10.2.10" % "test" exclude("com.typesafe.akka", "akka-streams_2.13")
    )
  
  //TODO: Make generic (or vert.x?)
  val TapirDependencies: Seq[ModuleID] =
    Seq(
      "com.softwaremill.sttp.tapir"   %% "tapir-akka-http-server"     % "1.2.0",
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui"           % "1.2.0",
      "com.softwaremill.sttp.tapir"   %% "tapir-openapi-docs"         % "1.2.0",
      "com.softwaremill.sttp.apispec" %% "openapi-circe-yaml"         % "0.3.1",
      "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"           % "1.2.0",
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"          % "1.2.0",
      
      "com.softwaremill.sttp.client3" %% "core"                       % "3.8.3",
      "com.softwaremill.sttp.client3" %% "akka-http-backend"          % "3.8.3"
    ) ++ AkkaDependencies
  
  val SmtpDependencies: Seq[ModuleID] =
    Seq(
      "javax"       % "javaee-api"  % "8.0.1",
      "javax.mail"  % "mail"        % "1.4.7",
      "org.jsoup"   % "jsoup"       % "1.15.3"
    )

  val JwtDependencies: Seq[ModuleID] =
    Seq("com.github.jwt-scala" %% "jwt-circe" % "9.1.1")

  val RedisDependencies: Seq[ModuleID] =
    Seq("redis.clients" % "jedis" % "4.3.1")

  val SlickDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.slick"                 %% "slick-hikaricp"      % "3.4.1",
      "com.typesafe.slick"                 %% "slick"               % "3.4.1",
      "io.github.nafg.slick-migration-api" %% "slick-migration-api" % "0.9.0"
    )

  val TypesafeConfigDependencies: Seq[ModuleID] =
    Seq("com.typesafe" % "config" % "1.4.2")

  val CirceDependencies: Seq[ModuleID] =
    Seq(
      "io.circe" %% "circe-core"    % "0.14.3",
      "io.circe" %% "circe-generic" % "0.14.3",
      "io.circe" %% "circe-parser"  % "0.14.3"
    )

  val ScalaTest: Seq[ModuleID] =
    Seq(
      "org.scalatest"     %% "scalatest" % "3.2.14" % "test",
      "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % "test",
      "org.scalacheck"    %% "scalacheck" % "1.17.0" % "test"
    )

  val CommonsDependencies: Seq[ModuleID] =
    Seq("org.apache.commons" % "commons-lang3" % "3.12.0")
  
  val ScalaLoggingDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.scala-logging" %% "scala-logging"           % "3.9.5",
      "net.logstash.logback"       % "logstash-logback-encoder" % "7.2"
    )

  val FluentLoggingDependencies: Seq[ModuleID] =
    Seq("org.fluentd" % "fluent-logger" % "0.3.4")
  
  val GooglePubSubDependencies: Seq[ModuleID] =
    Seq(
      "com.google.cloud" % "google-cloud-pubsub"  % "1.120.24"
    )
}
