name := """play"""

libraryDependencies ++=
  Dependencies.CompressorDependencies ++
    Dependencies.PlayDependencies :+
    "commons-io" % "commons-io" % "2.5"