import sbt.librarymanagement.{DependencyBuilders, ModuleID}

object Dependencies extends DependencyBuilders {
  val AkkaHttpVersion: String =
    "10.1.7"

  val AkkaVersion: String =
    "2.5.21"

  val CompressorVersion: String =
    "1.5.2"

  val GuiceVersion: String =
    "4.1.0"

  val JedisVersion: String =
    "2.9.0"

  val JwtVersion: String =
    "1.2.2"

  val PlayVersion: String =
    "2.7.0"

  val PlayAhcVersion: String =
    "2.0.0-M2"

  val SlickVersion: String =
    "3.2.3"

  val CirceVersion: String =
    "0.10.0"

  val AkkaHttpDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.akka"  %% "akka-http"              % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-xml"          % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-caching"      % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-actor"             % AkkaVersion,
      "com.typesafe.akka"  %% "akka-slf4j"             % AkkaVersion,
      "com.typesafe.akka"  %% "akka-stream"            % AkkaVersion,
      "de.heikoseeberger"  %% "akka-http-circe"        % "1.25.2",

      "com.typesafe.akka" %% "akka-http-testkit"    % AkkaHttpVersion,
      "com.typesafe.akka" %% "akka-testkit"         % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream-testkit"  % AkkaVersion)

  val CompressorDependencies: Seq[ModuleID] =
    Seq(
      "com.googlecode.htmlcompressor" % "htmlcompressor" % CompressorVersion)

  val EmailDependencies: Seq[ModuleID] =
    Seq(
      "javax"       % "javaee-api"  % "7.0",
      "javax.mail"  % "mail"        % "1.4")

  val GuiceDependencies: Seq[ModuleID] =
    Seq(
      "com.google.inject.extensions"  % "guice-assistedinject"  % GuiceVersion,
      "net.codingwell"                %% "scala-guice"          % GuiceVersion)

  val JwtDependencies: Seq[ModuleID] =
    Seq(
      "io.igl" %% "jwt" % JwtVersion)

  val RedisDependencies: Seq[ModuleID] =
    Seq(
      "redis.clients" % "jedis" % JedisVersion)

  val SlickDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.slick" %% "slick-hikaricp"      % SlickVersion,
      "com.typesafe.slick" %% "slick"               % SlickVersion)

  val PlayWSDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.play" %% "play-ws"                % PlayVersion,
      "com.typesafe.play" %% "play-ahc-ws-standalone" % PlayAhcVersion)

  val PlayDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.play" %% "play" % PlayVersion) ++ PlayWSDependencies

  val TypesafeConfigDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe" % "config" % "1.2.1")

  val CirceDependencies: Seq[ModuleID] =
    Seq(
      "io.circe" %% "circe-core"    % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser"  % CirceVersion)
}
