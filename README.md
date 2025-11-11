# Byrde Commons

A collection of reusable Scala libraries providing common functionality for backend services and applications.

## Overview

This project provides five independent modules that can be used individually or together:

- **commons**: Core utilities, domain types, and implicit conversions
- **logging**: Structured logging abstractions with Logstash support
- **pubsub**: Pub/sub messaging with Google Cloud Pub/Sub implementation
- **redis-client**: Type-safe Redis client with automatic serialization
- **smtp**: Email sending with JavaMail implementation

## Installation

Add the GitHub Package Registry resolver to your `build.sbt`:

```scala
resolvers += "GitHub Package Registry" at "https://maven.pkg.github.com/Byrde/commons"
```

Then include the modules you need:

```scala
libraryDependencies ++= Seq(
  "org.byrde" %% "commons"      % "VERSION",
  "org.byrde" %% "logging"      % "VERSION",
  "org.byrde" %% "pubsub"       % "VERSION",
  "org.byrde" %% "redis-client" % "VERSION",
  "org.byrde" %% "smtp"         % "VERSION"
)
```

## Modules

### Commons

Core utilities including domain types (Email, Phone, Money, JWT, Coordinates), implicit conversions, and URI building.

[Full documentation](commons/README.md)

```scala
libraryDependencies += "org.byrde" %% "commons" % "VERSION"
```

### Logging

Structured logging abstractions built on scala-logging with Logstash encoder support.

[Full documentation](logging/README.md)

```scala
libraryDependencies += "org.byrde" %% "logging" % "VERSION"
```

### PubSub

Messaging abstraction with Google Cloud Pub/Sub implementation and in-memory test implementation.

[Full documentation](pubsub/README.md)

```scala
libraryDependencies += "org.byrde" %% "pubsub" % "VERSION"
```

### Redis Client

Type-safe Redis client with automatic JSON serialization using Circe.

[Full documentation](redis-client/README.md)

```scala
libraryDependencies += "org.byrde" %% "redis-client" % "VERSION"
```

### SMTP

Email client abstraction with JavaMail implementation for sending emails via SMTP.

[Full documentation](smtp/README.md)

```scala
libraryDependencies += "org.byrde" %% "smtp" % "VERSION"
```

## Building

Compile all modules:

```bash
sbt compile
```

Run tests:

```bash
sbt test
```

Package all modules:

```bash
sbt package
```

## Publishing

Publish to GitHub Package Registry:

```bash
sbt publish
```

Note: Requires GitHub credentials configured in `~/.sbt/.credentials`

## License

Licensed under the Apache License, Version 2.0. See LICENSE file for details.

Copyright © 2018-present Martin Allaire

## Author

Martin Allaire (martin@byrde.io)
