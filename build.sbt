name :=
  Option(
    System.getProperty("name"))
    .getOrElse("commons")

version :=
  Option(
    System.getProperty("version"))
    .getOrElse("1.0")

organization :=
  Option(
    System.getProperty("organization"))
    .getOrElse("org.byrde")

scalaVersion :=
  Option(
    System.getProperty("scalaVersion"))
    .getOrElse("2.12.6")

scalaModuleInfo ~=
  (_.map(_.withOverrideScalaVersion(true)))

lazy val root =
  project in file(".")

resolvers ++=
  Seq(
    "byrdelibraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/",
    "pk11 repo" at "http://pk11-scratch.googlecode.com/svn/trunk",
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases")

val utils =
  Seq(
    "io.igl" %% "jwt" % "1.2.2",
    "org.joda" % "joda-convert" % "1.8.1",
    "commons-io" % "commons-io" % "2.5",
    "com.googlecode.htmlcompressor" % "htmlcompressor" % "1.5.2",
    "org.mozilla" % "rhino" % "1.7.7.1",
    "biz.source_code" % "base64coder" % "2010-12-19")

val sqlPersistence =
  Seq(
    "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",
    "com.typesafe.slick" %% "slick" % "3.2.3")

val redis =
  Seq(
    "org.byrde" %% "sedis" % "7",
    "redis.clients" % "jedis" % "2.4.2")

val play =
  Seq(
    "com.typesafe.play" %% "play" % "2.6.15",
    "com.typesafe.play" %% "play-ws" % "2.6.15",
    "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.0-M2",
    "com.google.inject" % "guice" % "4.2.0")

val mail =
  Seq(
    "javax" % "javaee-api" % "7.0",
    "javax.mail" % "mail" % "1.4")

libraryDependencies ++=
  utils ++
    play ++
    mail ++
    redis ++
    sqlPersistence :+
    "org.scalatest" %% "scalatest" % "3.0.1" % Test

unmanagedJars in Compile ++=
  ((baseDirectory.value / "lib") ** "*.jar").classpath

javacOptions ++=
  Seq(
    "-source", "1.8",
    "-target", "1.8",
    "-Xlint:unchecked",
    "-encoding", "UTF-8")

scalacOptions ++=
  Seq(
    "-unchecked",
    "-deprecation",
    "-Xlint",
    "-Ywarn-dead-code",
    "-language:_",
    "-target:jvm-1.8",
    "-encoding", "UTF-8")