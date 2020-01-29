import sbt.librarymanagement.{DependencyBuilders, ModuleID}

object Dependencies extends DependencyBuilders {
  val AkkaHttpVersion: String =
    "10.1.11"

  val AkkaVersion: String =
    "2.6.1"

  val PlayVersion: String =
    "2.8.0"

  val SlickVersion: String =
    "3.3.2"

  val CirceVersion: String =
    "0.12.3"

  val Zio: Seq[ModuleID] =
    Seq(
      "dev.zio" %% "zio" % "1.0.0-RC17"
    )

  val AkkaHttpDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.akka"  %% "akka-http"              % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-xml"          % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-caching"      % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-actor"             % AkkaVersion,
      "com.typesafe.akka"  %% "akka-slf4j"             % AkkaVersion,
      "com.typesafe.akka"  %% "akka-stream"            % AkkaVersion,
      "de.heikoseeberger"  %% "akka-http-circe"        % "1.30.0",

      "com.typesafe.akka" %% "akka-http-testkit"    % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-testkit"         % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-testkit"  % AkkaVersion
    )

  val CompressorDependencies: Seq[ModuleID] =
    Seq(
      "com.googlecode.htmlcompressor" % "htmlcompressor" % "1.5.2"
    )

  val EmailDependencies: Seq[ModuleID] =
    Seq(
      "javax"       % "javaee-api"  % "7.0",
      "javax.mail"  % "mail"        % "1.4"
    )

  val GuiceDependencies: Seq[ModuleID] =
    Seq(
      "com.google.inject.extensions"  % "guice-assistedinject"  % "4.2.2",
      "net.codingwell"                %% "scala-guice"          % "4.2.6"
    )

  val JwtDependencies: Seq[ModuleID] =
    Seq(
      "com.pauldijou" %% "jwt-circe" % "4.2.0"
    )

  val RedisDependencies: Seq[ModuleID] =
    Seq(
      "redis.clients" % "jedis" % "2.9.0"
    )

  val SlickDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.slick" %% "slick-hikaricp"      % SlickVersion,
      "io.github.nafg"     %% "slick-migration-api" % "0.7.0",
      "com.typesafe.slick" %% "slick"               % SlickVersion
    )

  val PlayWSDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.play" %% "play-ws" % PlayVersion
    )

  val PlayDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.play" %% "play" % PlayVersion
    ) ++ PlayWSDependencies

  val TypesafeConfigDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe" % "config" % "1.2.1"
    )

  val CirceDependencies: Seq[ModuleID] =
    Seq(
      "io.circe" %% "circe-core"    % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser"  % CirceVersion
    )

  val ScalaTest: Seq[ModuleID] =
    Seq(
      "org.scalatest"        %% "scalatest" % "3.1.0" % "test",
      "org.scalatestplus"    %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
      "org.scalacheck"       %% "scalacheck" % "1.14.0"
    )
}
