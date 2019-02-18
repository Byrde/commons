name := """redis"""

libraryDependencies ++=
  Dependencies.RedisDependencies ++
    Dependencies.TypesafeConfigDependencies :+
    "biz.source_code" % "base64coder" % "2010-12-19"