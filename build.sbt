val commons =
  Seq(
    version :=
      Option(
        System.getProperty("version"))
        .getOrElse("1.0"),
    organization :=
      Option(
        System.getProperty("organization"))
        .getOrElse("org.byrde"),
    scalaVersion :=
      Option(
        System.getProperty("scalaVersion"))
        .getOrElse("2.12.6"),
    scalaModuleInfo ~=
      (_.map(_.withOverrideScalaVersion(true))))

lazy val clients =
  project
    .dependsOn(
      `service-response`,
      uri,
      utils
    )
    .settings(commons)

lazy val email =
  project
    .dependsOn(`service-response`)
    .settings(commons)

lazy val jwt =
  project
    .settings(commons)

lazy val play =
  project
    .dependsOn(
      jwt,
      `service-response`,
      uri,
      utils
    )
    .settings(commons)

lazy val redis =
  project
    .settings(commons)

lazy val `service-response` =
  project
    .settings(commons)

lazy val slick =
  project
    .settings(commons)

lazy val uri =
  project
    .settings(commons)

lazy val utils =
  project
    .dependsOn(uri)
    .settings(commons)

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