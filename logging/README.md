# Logging [![Latest Version @ Cloudsmith](https://api-prd.cloudsmith.io/badges/version/byrde/libraries/maven/logging_2.13/latest/x/?render=true)](https://cloudsmith.io/~byrde/repos/libraries/packages/detail/maven/logging_2.13/latest/)

Absctraction for a Zio Logging Client.

## How to install

* add to your dependencies library dependencies:
```libraryDependencies += "org.byrde" %% "logging" % "VERSION"```

* add this resolver to your resolvers dependencies:
```resolvers += "byrde-libraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/"```

## Quickstart
This library defines a common interface for implementing a logging client.
Compatible implementations:
- [akka-logging](https://github.com/Byrde/commons/tree/master/akka-logging)
- [play-logging](https://github.com/Byrde/commons/tree/master/play-logging)

#### Usage
```scala
class MyLogger() extends Logger {

  def debug(msg: String): Unit = ???
  
  def info(msg: String): Unit
  
  def warning(msg: String): Unit
  
  def error(msg: String): Unit
  
  def error(msg: String, cause: Throwable): Unit

}

class MyClass() extends Logging

val logger = new MyLogger()

val clazz = new MyClass()
```

#### Info
```
case class MyMessage(message: String)

implicit def encoder: Encoder[MyMessage] = ???

val message = MyMessage("Hello World!")

clazz.info(message).provide(logger)
```
