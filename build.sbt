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
		.getOrElse("2.11.11")

lazy val root =
	project in file(".")

val utils =
	Seq (
		"io.igl" 												%% "jwt" 						% "1.2.0",
		"org.scalaz" 										%% "scalaz-core" 		% "7.2.8",
		"org.joda" 											%  "joda-convert" 	% "1.8.1",
		"commons-io" 										%  "commons-io" 		% "2.5",
		"com.googlecode.htmlcompressor" %  "htmlcompressor" % "1.5.2",
		"org.mozilla" 									%  "rhino" 					% "1.7.7.1")

val play =
	Seq (
		"com.typesafe.play" % "play_2.11" 		% "2.5.18",
		"com.typesafe.play" % "play-ws_2.11" 	% "2.5.18",
		"com.google.inject" % "guice" 				% "4.1.0")

val amazon =
	Seq (
		"com.amazonaws" % "aws-java-sdk" % "1.11.33")

val mail =
	Seq (
		"javax" 			% "javaee-api" 	% "7.0",
		"javax.mail" 	% "mail" 				% "1.4")

libraryDependencies ++=
	utils ++
	play ++
	amazon ++
	mail

unmanagedJars in Compile ++=
	((baseDirectory.value / "lib") ** "*.jar").classpath

javacOptions ++=
	Seq (
		"-source", "1.8",
		"-target", "1.8",
		"-Xlint:unchecked",
		"-encoding", "UTF-8")

scalacOptions ++=
	Seq (
		"-unchecked",
		"-deprecation",
		"-Xlint",
		"-Ywarn-dead-code",
		"-language:_",
		"-target:jvm-1.8",
		"-encoding", "UTF-8")