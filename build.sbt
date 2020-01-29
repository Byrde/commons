val RootSettings =
  Seq(
    name :=
      Option(
        System.getProperty("name"))
        .getOrElse("commons"),
    publish := {},
    publishLocal := {})

val CommonsSettings =
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
        .getOrElse("2.13.1"),
    scalaModuleInfo ~=
      (_.map(_.withOverrideScalaVersion(true))),
    resolvers ++=
      Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.bintrayRepo("hseeberger", "maven"),
        Resolver.jcenterRepo),
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
    publishMavenStyle := true,
    developers +=
      Developer(
        "Alfapalooza",
        "Martin Allaire",
        "martin@byrde.io",
        new URL("http://linkedin.com/allama")))

val auth =
  project
    .settings(CommonsSettings)

val jwt =
  project
    .settings(CommonsSettings)

val redis =
  project
    .settings(CommonsSettings)

val `service-response-circe` =
  project
    .settings(CommonsSettings)

val slick =
  project
    .settings(CommonsSettings)

val uri =
  project
    .settings(CommonsSettings)

val email =
  project
    .settings(CommonsSettings)

val `logging-circe` =
  project
    .dependsOn(`service-response-circe`)
    .settings(CommonsSettings)

val utils =
  project
    .dependsOn(uri)
    .settings(CommonsSettings)

val `clients-circe` =
  project
    .dependsOn(
      `service-response-circe`,
      utils
    )
    .settings(CommonsSettings)

val play =
  project
    .dependsOn(
      jwt,
      utils
    )
    .settings(CommonsSettings)

val `akka-http` =
  project
    .dependsOn(
      `logging-circe`,
      `service-response-circe`,
      utils
    )
    .settings(CommonsSettings)

val root =
  Project("commons", file("."))
    .settings(RootSettings)
    .aggregate(
      `akka-http`,
      auth,
      `clients-circe`,
      email,
      jwt,
      `logging-circe`,
      play,
      redis,
      `service-response-circe`,
      slick,
      uri,
      utils
    )
