# Play Http Client [![Latest Version @ Cloudsmith](https://api-prd.cloudsmith.io/badges/version/byrde/libraries/maven/play-client_2.13/latest/x/?render=true)](https://cloudsmith.io/~byrde/repos/libraries/packages/detail/maven/play-client_2.13/latest/)

`play-ahc-ws-standalone` implementation of the [http-client](https://github.com/Byrde/commons/tree/master/http-client) library

## How to install

* add to your dependencies library dependencies:
```libraryDependencies += "org.byrde" %% "play-client" % "VERSION"```

* add this resolver to your resolvers dependencies:
```resolvers += "byrde-libraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/"```

## Quickstart
This library is compatible implementation of the [http-client](https://github.com/Byrde/commons/tree/master/http-client) library

#### Usage
```scala
val system = ActorSystem()

val config = PlayConfig(new SimpleConfig())

val service = new PlayService(StandaloneAhcWSClient()(Materializer(system)))(config)

val client = new PlayClient(service)
```

#### Get
```
import org.byrde.client.http.play.implicits._
client.get(Request[Unit](Path("/ping")))
```

#### Post
```
client.post[Json, Json](Request[Json](Path("/ping"), Some(Json.obj("ping" -> Json.True))))
```

## Config Sample
```yaml
{
   protocol: "http"
   host: "localhost"
   port: 80
   client-id: "client-id"
   client-token: "client-token"
   call-timeout: 2000
}
```
