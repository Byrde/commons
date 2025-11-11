name := """redis-client"""

description := "Type-safe Redis client abstraction with Jedis implementation. https://github.com/Byrde/commons/tree/main/redis-client"

homepage := Some(url("https://github.com/Byrde/commons/tree/main/redis-client"))

libraryDependencies ++=
  Dependencies.CirceDependencies ++
    Dependencies.TypesafeConfigDependencies ++
    Dependencies.CommonsDependencies ++
    Dependencies.RedisDependencies
