val RootSettings =
  Seq(
    name := Option(System.getProperty("name")).getOrElse("commons"),
    publish := {},
    publishLocal := {}
  )

val CommonsSettings =
  Seq(
    version := Option(System.getProperty("version")).getOrElse("0.1-SNAPSHOT"),
    organization := "org.byrde",
    scalaVersion := "2.13.6",
    scalaModuleInfo ~=
      (_.map(_.withOverrideScalaVersion(true))),
    resolvers ++=
      Seq(
        Resolver.sonatypeRepo("releases"),
        Resolver.bintrayRepo("hseeberger", "maven"),
        Resolver.jcenterRepo
      ),
    javacOptions ++=
      Seq(
        "-source", "1.8",
        "-target", "1.8",
        "-Xlint:unchecked",
        "-encoding", "UTF-8"
      ),
    scalacOptions ++=
      Seq(
        "-unchecked",
        "-deprecation",
        "-Xlint",
        "-Ywarn-dead-code",
        "-language:_",
        "-target:jvm-1.8",
        "-encoding", "UTF-8",
        "-Wconf:cat=lint-byname-implicit:silent",
        "-Ymacro-annotations",
        "-Xfatal-warnings",
      ),
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
    credentials += Credentials(Path.userHome / ".sbt" / ".credentials"),
    pomIncludeRepository := (_ => false),
    startYear := Some(2018),
    licenses := Seq(("Apache 2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))),
    publishTo := Some("GitHubPackages" at "https://maven.pkg.github.com/Byrde/commons"),
    publishMavenStyle := true,
    developers +=
      Developer(
        "mallaire77",
        "Martin Allaire",
        "martin@byrde.io",
        new URL("http://linkedin.com/allama")
      )
  )

val `jwt` =
  project.settings(CommonsSettings)

val slick =
  project.settings(CommonsSettings)

val uri =
  project.settings(CommonsSettings)

val logging =
  project.settings(CommonsSettings)

val `scala-logging` =
  project
    .dependsOn(logging)
    .settings(CommonsSettings)

val `fluent-logging` =
  project
    .dependsOn(logging)
    .settings(CommonsSettings)

val support =
  project
    .dependsOn(uri)
    .settings(CommonsSettings)

val pubsub  =
  project
    .dependsOn(
      logging,
      support
    )
    .settings(CommonsSettings)

val smtp =
  project
    .dependsOn(support)
    .settings(CommonsSettings)

val `redis-client` =
  project
    .dependsOn(support)
    .settings(CommonsSettings)

val `jedis-client` =
  project
    .dependsOn(`redis-client`)
    .settings(CommonsSettings)

val `http` =
  project
    .dependsOn(
      logging,
      support
    )
    .settings(CommonsSettings)

val root =
  Project("commons", file("."))
    .settings(RootSettings)
    .aggregate(
      `http`,
      pubsub,
      smtp,
      `jwt`,
      logging,
      `scala-logging`,
      `fluent-logging`,
      `redis-client`,
      `jedis-client`,
      slick,
      uri,
      support,
    )
