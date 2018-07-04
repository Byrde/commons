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
    .getOrElse("2.11.12")
scalaModuleInfo ~=
  (_.map(_.withOverrideScalaVersion(true)))

lazy val root =
  project in file(".")

val utils =
  Seq(
    "io.igl" %% "jwt" % "1.2.2",
    "org.scalaz" %% "scalaz-core" % "7.2.8",
    "org.joda" % "joda-convert" % "1.8.1",
    "commons-io" % "commons-io" % "2.5",
    "com.googlecode.htmlcompressor" % "htmlcompressor" % "1.5.2",
    "org.mozilla" % "rhino" % "1.7.7.1")

val sqlPersistence =
  Seq(
    "com.typesafe.slick" % "slick-hikaricp_2.11" % "3.2.3",
    "com.typesafe.slick" % "slick_2.11" % "3.2.3")

val play =
  Seq(
    "com.typesafe.play" % "play_2.11" % "2.6.15",
    "com.typesafe.play" % "play-ws_2.11" % "2.6.15",
    "com.google.inject" % "guice" % "4.2.0")

val mail =
  Seq(
    "javax" % "javaee-api" % "7.0",
    "javax.mail" % "mail" % "1.4")

libraryDependencies ++=
  utils ++
    play ++
    mail ++
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