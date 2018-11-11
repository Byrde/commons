import sbt.librarymanagement.DependencyBuilders

object Dependencies extends DependencyBuilders {
  val AkkaHttpVersion =
    "10.1.0"

  val CompressorVersion =
    "1.5.2"

  val GuiceVersion =
    "4.1.0"

  val JedisVersion =
    "2.9.0"

  val JwtVersion =
    "1.2.2"

  val PlayVersion =
    "2.6.20"

  val PlayAhcVersion =
    "2.0.0-RC1"

  val SlickVersion =
    "3.2.3"

  val AkkaHttpDependencies =
    Seq(
      "com.typesafe.akka" %% "akka-http"            % AkkaHttpVersion,
      "de.heikoseeberger" %% "akka-http-play-json"  % "1.17.0")

  val CompressorDependencies =
    Seq(
      "com.googlecode.htmlcompressor" % "htmlcompressor" % CompressorVersion)

  val EmailDependencies =
    Seq(
      "javax"       % "javaee-api"  % "7.0",
      "javax.mail"  % "mail"        % "1.4")

  val GuiceDependencies =
    Seq(
      "com.google.inject.extensions"  % "guice-assistedinject"  % GuiceVersion,
      "net.codingwell"                %% "scala-guice"          % GuiceVersion)

  val JwtDependencies =
    Seq(
      "io.igl" %% "jwt" % JwtVersion)

  val RedisDependencies =
    Seq(
      "redis.clients" % "jedis" % JedisVersion)

  val SlickDependencies =
    Seq(
      "com.typesafe.slick" %% "slick-hikaricp"      % SlickVersion,
      "com.typesafe.slick" %% "slick"               % SlickVersion)

  val PlayDependencies =
    Seq(
      "com.typesafe.play" %% "play"                   % PlayVersion,
      "com.typesafe.play" %% "play-ws"                % PlayVersion,
      "com.typesafe.play" %% "play-ahc-ws-standalone" % PlayAhcVersion)
}
