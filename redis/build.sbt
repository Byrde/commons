name := """redis"""

resolvers ++=
  Seq(
    "byrdelibraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/")

libraryDependencies ++=
  Dependencies.RedisDependencies ++
    Dependencies.TypesafeConfigDependencies :+
    "biz.source_code" % "base64coder" % "2010-12-19"

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