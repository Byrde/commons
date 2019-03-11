name := """play"""

libraryDependencies ++=
  Dependencies.CompressorDependencies ++
    Dependencies.PlayDependencies ++
    Dependencies.CirceDependencies :+
    "commons-io" % "commons-io" % "2.5"