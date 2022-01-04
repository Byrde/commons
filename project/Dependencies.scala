import sbt.librarymanagement.{DependencyBuilders, ModuleID}

object Dependencies extends DependencyBuilders {
  //TODO: Remove Akka
  val AkkaHttpVersion: String =
    "10.2.7"
  
  //TODO: Remove Akka
  val AkkaVersion: String =
    "2.6.17"
  
  //TODO: Jooq library
  val SlickVersion: String =
    "3.3.3"
  
  //TODO: Jooq library
  val SlickMigrationsVersion: String =
    "0.8.2"

  val CirceVersion: String =
    "0.14.1"
  
  val TapirVersion: String =
    "0.19.1"
  
  val SttpVersion: String =
    "3.3.18"
  
  //TODO: Use vanilla Java libraries (pubsub, gcs, etc..)
  val AlpakkaVersion: String =
    "3.0.4"

  val AkkaDependencies =
    Seq(
      "com.typesafe.akka"  %% "akka-actor"          % AkkaVersion,
      "com.typesafe.akka"  %% "akka-slf4j"          % AkkaVersion,
      "com.typesafe.akka"  %% "akka-stream"         % AkkaVersion,
      "com.typesafe.akka"  %% "akka-testkit"        % AkkaVersion,
      "com.typesafe.akka"  %% "akka-stream-testkit" % AkkaVersion
    )

  val AlpakkaGCS =
    //TODO: Use vanilla Java libraries (pubsub, gcs, etc..)
    Seq("com.lightbend.akka" %% "akka-stream-alpakka-google-cloud-storage" % AlpakkaVersion)
  
  val AlpakkaPubSub =
    //TODO: Use vanilla Java libraries (pubsub, gcs, etc..)
    Seq("com.lightbend.akka" %% "akka-stream-alpakka-google-cloud-pub-sub" % AlpakkaVersion)
  
  val TapirDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.akka"             %% "akka-http-testkit"          % AkkaHttpVersion % "test" exclude("com.typesafe.akka", "akka-streams_2.13"),
      //TODO: Move to vert.x
      "com.softwaremill.sttp.tapir"   %% "tapir-akka-http-server"     % TapirVersion exclude("com.typesafe.akka", "akka-stream_2.13"),
      "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui"           % TapirVersion exclude("com.typesafe.akka", "akka-stream_2.13"),
      "com.softwaremill.sttp.tapir"   %% "tapir-openapi-docs"         % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-openapi-circe-yaml"   % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"           % TapirVersion,
      "com.softwaremill.sttp.tapir"   %% "tapir-sttp-client"          % TapirVersion,
      //TODO: Move to vert.x
      "com.softwaremill.sttp.client3" %% "core"                       % SttpVersion,
      "com.softwaremill.sttp.client3" %% "akka-http-backend"          % SttpVersion
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
    Seq("redis.clients" % "jedis" % "3.7.0")

  val SlickDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.slick"                 %% "slick-hikaricp"      % SlickVersion,
      "com.typesafe.slick"                 %% "slick"               % SlickVersion,
      "io.github.nafg.slick-migration-api" %% "slick-migration-api" % SlickMigrationsVersion
    )

  val TypesafeConfigDependencies: Seq[ModuleID] =
    Seq("com.typesafe" % "config" % "1.4.1")

  val CirceDependencies: Seq[ModuleID] =
    Seq(
      "io.circe" %% "circe-core"    % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser"  % CirceVersion
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
      "net.logstash.logback"       % "logstash-logback-encoder" % "7.0"
    )
}
