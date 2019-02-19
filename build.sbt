val RootSettings =
  Seq(
    name :=
      Option(
        System.getProperty("name"))
        .getOrElse("commons"),
    publish := {},
    publishLocal := {})

val commons =
  Seq(
    version :=
      Option(
        System.getProperty("version"))
        .getOrElse("SNAPSHOT"),
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
        new URL("http://linkedin.com/allama")),
    publishMavenStyle := true)

val jwt =
  project
    .settings(commons)

val logging =
  project
    .settings(commons)

val `logging-circe` =
  project
    .settings(commons)

val redis =
  project
    .settings(commons)

val `service-response` =
  project
    .settings(commons)

val `service-response-circe` =
  project
    .settings(commons)

val slick =
  project
    .settings(commons)

val uri =
  project
    .settings(commons)

val email =
  project
    .dependsOn(`service-response`)
    .settings(commons)

val utils =
  project
    .dependsOn(uri)
    .settings(commons)

val play =
  project
    .dependsOn(
      jwt,
      `service-response`,
      uri,
      utils
    )
    .settings(commons)

val clients =
  project
    .dependsOn(
      `service-response`,
      uri,
      utils
    )
    .settings(commons)

val `clients-circe` =
  project
    .dependsOn(
      `service-response-circe`,
      uri,
      utils
    )
    .settings(commons)

val `akka-http` =
  project
    .dependsOn(
      `logging-circe`,
      `service-response-circe`,
      uri,
      utils
    )
    .settings(commons)

val root =
  Project("commons", file("."))
    .settings(RootSettings)
    .aggregate(
      `akka-http`,
      clients,
      `clients-circe`,
      email,
      jwt,
      logging,
      play,
      redis,
      `service-response`,
      `service-response-circe`,
      slick,
      uri,
      utils)