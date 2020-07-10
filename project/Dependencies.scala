import sbt.librarymanagement.{DependencyBuilders, ModuleID}

object Dependencies extends DependencyBuilders {
  val AkkaHttpVersion: String =
    "10.1.11"

  val AkkaVersion: String =
    "2.5.31"

  val PlayVersion: String =
    "2.7.4"

  val SlickVersion: String =
    "3.3.2"

  val CirceVersion: String =
    "0.13.0"

  val Zio: Seq[ModuleID] =
    Seq("dev.zio" %% "zio" % "1.0.0-RC20")

  val AkkaDependencies =
    Seq(
      "com.typesafe.akka"  %% "akka-actor"          % AkkaVersion,
      "com.typesafe.akka"  %% "akka-slf4j"          % AkkaVersion,
      "com.typesafe.akka"  %% "akka-stream"         % AkkaVersion,
      "com.typesafe.akka"  %% "akka-testkit"        % AkkaVersion,
      "com.typesafe.akka"  %% "akka-stream-testkit" % AkkaVersion
    )

  val AlpakkaGCS =
    Seq("com.lightbend.akka" %% "akka-stream-alpakka-google-cloud-storage" % "2.0.0")
  
  val AlpakkaPubSub =
    Seq("com.lightbend.akka" %% "akka-stream-alpakka-google-cloud-pub-sub" % "2.0.0")

  val AkkaHttpDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.akka"  %% "akka-http"              % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http2-support"     % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-xml"          % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-caching"      % AkkaHttpVersion,
      "com.typesafe.akka"  %% "akka-http-testkit"      % AkkaHttpVersion,
    ) ++ AkkaDependencies

  val CompressorDependencies: Seq[ModuleID] =
    Seq("com.googlecode.htmlcompressor" % "htmlcompressor" % "1.5.2")

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
    Seq("com.pauldijou" %% "jwt-circe" % "4.3.0")

  val RedisDependencies: Seq[ModuleID] =
    Seq("redis.clients" % "jedis" % "2.9.0")

  val SlickDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.slick" %% "slick-hikaricp"      % SlickVersion,
      "com.typesafe.slick" %% "slick"               % SlickVersion,
      "io.github.nafg"     %% "slick-migration-api" % "0.7.0",
    )

  val PlayWSDependencies: Seq[ModuleID] =
    Seq(
      "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.8",
      "com.typesafe.play" %% "play-ws" % PlayVersion
    )

  val PlayDependencies: Seq[ModuleID] =
    Seq("com.typesafe.play" %% "play" % PlayVersion) ++ PlayWSDependencies

  val TypesafeConfigDependencies: Seq[ModuleID] =
    Seq("com.typesafe" % "config" % "1.3.3")

  val CirceDependencies: Seq[ModuleID] =
    Seq(
      "io.circe" %% "circe-core"    % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser"  % CirceVersion
    )

  val ScalaTest: Seq[ModuleID] =
    Seq(
      "org.scalatest"        %% "scalatest" % "3.1.0" % "test",
      "org.scalatestplus"    %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % "test",
      "org.scalacheck"       %% "scalacheck" % "1.14.0" % "test"
    )

  val CommonsDependencies: Seq[ModuleID] =
    Seq("commons-codec" % "commons-codec" % "1.13")
}
