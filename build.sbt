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
        new URL("http://linkedin.com/allama")),
    libraryDependencies ++=
      Seq(
        compilerPlugin("com.github.ghik" %% "silencer-plugin" % "1.3.1"),
        "com.github.ghik" %% "silencer-lib" % "1.3.1" % Provided))

val jwt =
  project
    .settings(CommonsSettings)

val logging =
  project
    .settings(CommonsSettings)

val `logging-circe` =
  project
    .settings(CommonsSettings)

val redis =
  project
    .settings(CommonsSettings)

val `service-response` =
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
    .dependsOn(`service-response`)
    .settings(CommonsSettings)

val utils =
  project
    .dependsOn(uri)
    .settings(CommonsSettings)

val play =
  project
    .dependsOn(
      jwt,
      `service-response`,
      uri,
      utils
    )
    .settings(CommonsSettings)

val clients =
  project
    .dependsOn(
      `service-response`,
      uri,
      utils
    )
    .settings(CommonsSettings)

val `clients-circe` =
  project
    .dependsOn(
      `service-response-circe`,
      uri,
      utils
    )
    .settings(CommonsSettings)

val `akka-http` =
  project
    .dependsOn(
      `logging-circe`,
      `service-response-circe`,
      uri,
      utils
    )
    .settings(CommonsSettings)

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
      `logging-circe`,
      play,
      redis,
      `service-response`,
      `service-response-circe`,
      slick,
      uri,
      utils)