# Http

Library to scaffold a Akka Http based Tapir server.

## How to install

* add to your dependencies library dependencies:
```libraryDependencies += "org.byrde" %% "tapir" % "VERSION"```

* add this resolver to your resolvers dependencies:
```resolvers += "byrde-libraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/"```

## Quickstart
When building an AkkaHttp server, you can extend `org.byrde.http.server.HttpServer` for out of the box configurations on things
such as: 
- Request & Response Handling
  - CORS
  - Standardized requests
  - Standardized responses
  - Logging
- Documentation
  - OpenAPI of all project routes
  - Swagger documentation of all project routes
- Exception Handling
  - Response formatting
  - Error code tagging
  - Logging