# Logging

Absctraction for a logging client.

## How to install

* add to your dependencies library dependencies:
```libraryDependencies += "org.byrde" %% "logging" % "VERSION"```

* add this resolver to your resolvers dependencies:
```resolvers += "byrde-libraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/"```

## Quickstart
This library defines a common interface for implementing a logging client.
Compatible implementations:
- [scala-logging](https://github.com/Byrde/commons/tree/master/scala-logging)
