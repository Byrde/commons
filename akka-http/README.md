# Akka-HTTP [![Latest Version @ Cloudsmith](https://api-prd.cloudsmith.io/badges/version/byrde/libraries/maven/akka-http_2.13/latest/xg=org.byrde/?render=true&badge_token=gAAAAABexUBgmQEj_veAiFyniTqh54vt4zkzbBUbyf3KRCSfDnaW9niImRWlPs2V24KmAUWgvIDtLphcIHddhdEihf8rTsLdLAvu5sjyOnG-cQcc55h6lpQ%3D)](https://cloudsmith.io/~byrde/repos/libraries/packages/detail/maven/akka-http_2.13/latest/xg=org.byrde/)

Library to scaffold an AkkaHttp server.

## How to install

* add to your dependencies library dependencies:
```libraryDependencies += "org.byrde" %% "akka-http" % "VERSION"```

* add this resolver to your resolvers dependencies:
```resolvers += "byrde-libraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/"```


## Quickstart
When building an AkkaHttp server, you can extend `org.byrde.akka.http.Server` for out of the box configurations on things
such as: 
- Request Handling
  - CORS
  - Method Rejections
  - Response Formatting
  - Logging
- Exception Handling 
  - Response Formatting
  - Error Code Tagging
  - Logging
- Rejection Handling 
  - Response Formatting
  - Error Code Tagging
  - Logging

#### Response Formatting
Response formatting refers to structuring responses according to the [`service-response`](https://github.com/Byrde/commons/tree/master/service-response)
library. Which includes error code tagging for easily handling client/server errors on the client.

#### Logging
Logging requires a implementation of the [`logging`](https://github.com/Byrde/commons/tree/master/logging) library.

#### Implementation
Here's an example implementation:
```scala
object Server extends org.byrde.akka.http.Server[RuntimeModules, ModulesProvider] {
  override implicit def global: ExecutionContextExecutor =
    ThreadPools.Global

  implicit lazy val provider: ConcreteModulesProvider =
    new ConcreteModulesProvider()

  private implicit val materializer: Materializer =
    Materializer(system)

  private lazy val handlers: Set[RejectionHandler] =
    Set.empty

  private lazy val v1: Route =
    path("v1" / "ping") {
      complete("pong")
    }

  private lazy val builder: ConcreteRuntimeModules.type =
    ConcreteRuntimeModules

  private lazy val routes: Route =
    v1

  Http()
    .bindAndHandle(
      handled,
      provider.config.interface,
      provider.config.port
    )
}
```

## Marshalling Responses
To easily handle serialization of different response types you can extend `org.byrde.akka.http.support.ResponseSupport` and use
a variety of convenience functions for serializing to:
- Send an Acknowledgement response
- Serialize to JSON
- Serialize to `org.byrde.service.response.ServiceResponse[T]`