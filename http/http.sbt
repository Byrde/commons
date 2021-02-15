name := """http"""

libraryDependencies ++=
  Dependencies.TapirDependencies ++
    Dependencies.ScalaTest :+
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"