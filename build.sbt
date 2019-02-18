import sbt.Keys.scalacOptions

name :=
  Option(
    System.getProperty("name"))
    .getOrElse("commons")

val commons =
  Seq(
    version :=
      System.getProperty("version"),
    organization :=
      Option(
        System.getProperty("organization"))
        .getOrElse("org.byrde"),
    scalaVersion :=
      Option(
        System.getProperty("scalaVersion"))
        .getOrElse("2.12.6"),
    scalaModuleInfo ~=
      (_.map(_.withOverrideScalaVersion(true))),
    resolvers +=
      Resolver.bintrayRepo("hseeberger", "maven"),
    javacOptions ++=
      Seq(
        "-source", "1.8",
        "-target", "1.8",
        "-Xlint:unchecked",
        "-encoding", "UTF-8"),
    scalacOptions ++=
      Seq(
        "-unchecked",
        "-deprecation",
        "-Xlint",
        "-Ywarn-dead-code",
        "-language:_",
        "-target:jvm-1.8",
        "-encoding", "UTF-8"),
    packageOptions +=
      Package.ManifestAttributes(
        "Created-By" -> "Martin Allaire",
        "Built-By" -> System.getProperty("user.name"),
        "Build-Jdk" -> System.getProperty("java.version"),
        "Specification-Title" -> name.value,
        "Specification-Version" -> version.value,
        "Specification-Vendor" -> organization.value,
        "Implementation-Title" -> name.value,
        "Implementation-Version" -> version.value,
        "Implementation-Vendor-Id" -> organization.value,
        "Implementation-Vendor" -> organization.value
      ),
    credentials +=
      Credentials(Path.userHome / ".sbt" / ".credentials"),
    pomIncludeRepository :=
      (_ => false),
    startYear :=
      Some(2018),
    licenses :=
      Seq(("Apache 2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))),
    publishTo :=
      Some("Cloudsmith API" at "https://maven.cloudsmith.io/byrde/libraries/"),
    developers +=
      Developer(
        "Alfapalooza",
        "Martin Allaire",
        "martin@byrde.io",
        new URL("http://linkedin.com/allama")))

lazy val `akka-http` =
  project
    .dependsOn(
      logging,
      `service-response-circe`,
      uri,
      utils
    )
    .settings(commons)

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

lazy val logging =
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

lazy val `service-response-circe` =
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

overridePublishSettings