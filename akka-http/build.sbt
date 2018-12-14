import sbt.Resolver
name := """akka-http"""

resolvers +=
  Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++=
  Dependencies.AkkaDependencies ++ Dependencies.GuiceDependencies

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