name := """commons"""

description := "Core utilities and type definitions for Scala projects."

homepage := Some(url("https://github.com/Byrde/commons/tree/main/commons"))

libraryDependencies ++=
  Dependencies.JwtDependencies ++
    Dependencies.TypesafeConfigDependencies ++
    Dependencies.CirceDependencies ++
    Dependencies.ScalaTest
