import sbt.librarymanagement.{DependencyBuilders, ModuleID}

object Dependencies extends DependencyBuilders {
  //TODO: No more akka
  val AkkaDependencies =
    Seq(
      "com.typesafe.akka"  %% "akka-actor"          % "2.6.18",
      "com.typesafe.akka"  %% "akka-slf4j"          % "2.6.18",
      "com.typesafe.akka"  %% "akka-stream"         % "2.6.18",
      "com.typesafe.akka"  %% "akka-testkit"        % "2.6.18",
      "com.typesafe.akka"  %% "akka-stream-testkit" % "2.6.18"
    )
  
  //TODO: Make generic (or vert.x?)
  val TapirDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.akka"             %% "akka-http-testkit"          % "10.2.7" % "test" exclude("com.typesafe.akka", "akka-streams_2.13"),

      "com.softwaremill.sttp.tapir"   %% "tapir-akka-http-server"     % "0.19.3" exclude("com.typesafe.akka", "akka-stream_2.13"),
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui"           % "0.19.3" exclude("com.typesafe.akka", "akka-stream_2.13"),
      "com.softwaremill.sttp.tapir"   %% "tapir-openapi-docs"         % "0.19.3",
      "com.softwaremill.sttp.tapir"   %% "tapir-openapi-circe-yaml"   % "0.19.3",
      "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"           % "0.19.3",
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"          % "0.19.3",
      
      "com.softwaremill.sttp.client3" %% "core"                       % "3.3.18",
      "com.softwaremill.sttp.client3" %% "akka-http-backend"          % "3.3.18"
    ) ++ AkkaDependencies
  
  val SmtpDependencies: Seq[ModuleID] =
    Seq(
      "javax"       % "javaee-api"  % "8.0.1",
      "javax.mail"  % "mail"        % "1.4.7",
      "org.jsoup"   % "jsoup"       % "1.14.3"
    )

  val JwtDependencies: Seq[ModuleID] =
    Seq("com.github.jwt-scala" %% "jwt-circe" % "9.0.3")

  val RedisDependencies: Seq[ModuleID] =
    Seq("redis.clients" % "jedis" % "4.0.0")

  val SlickDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.slick"                 %% "slick-hikaricp"      % "3.3.3",
      "com.typesafe.slick"                 %% "slick"               % "3.3.3",
      "io.github.nafg.slick-migration-api" %% "slick-migration-api" % "0.8.2"
    )

  val TypesafeConfigDependencies: Seq[ModuleID] =
    Seq("com.typesafe" % "config" % "1.4.1")

  val CirceDependencies: Seq[ModuleID] =
    Seq(
      "io.circe" %% "circe-core"    % "0.14.1",
      "io.circe" %% "circe-generic" % "0.14.1",
      "io.circe" %% "circe-parser"  % "0.14.1"
    )

  val ScalaTest: Seq[ModuleID] =
    Seq(
      "org.scalatest"     %% "scalatest" % "3.2.9" % "test",
      "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % "test",
      "org.scalacheck"    %% "scalacheck" % "1.15.4" % "test"
    )

  val CommonsDependencies: Seq[ModuleID] =
    Seq("commons-codec" % "commons-codec" % "20041127.091804")
  
  val LoggingDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.scala-logging" %% "scala-logging"           % "3.9.4",
      "net.logstash.logback"       % "logstash-logback-encoder" % "7.0.1"
    )
  
  val GooglePubSubDependencies: Seq[ModuleID] =
    Seq(
      "com.google.cloud" % "google-cloud-pubsub"  % "1.115.0"
    )
}
