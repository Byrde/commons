# Jedis Client [![Latest Version @ Cloudsmith](https://api-prd.cloudsmith.io/badges/version/byrde/libraries/maven/jedis-client_2.13/latest/x/?render=true)](https://cloudsmith.io/~byrde/repos/libraries/packages/detail/maven/jedis-client_2.13/latest/)

Jedis client implementation and wrapper for the [redis-client](https://github.com/Byrde/commons/tree/master/redis-client) library

## How to install

* add to your dependencies library dependencies:
```libraryDependencies += "org.byrde" %% "jedis-client" % "VERSION"```

* add this resolver to your resolvers dependencies:
```resolvers += "byrde-libraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/"```

## Quickstart
This library is compatible implementation of the [redis-client](https://github.com/Byrde/commons/tree/master/redis-client) library

#### Usage
```scala
val config = JedisConfig(new SimpleConfig())

val service = new JedisService(config)

val client = new JedisClient(service)
```

#### Get
```
client.get[String]("example")
```

#### Set
```
client.set[String]("example", "my-value")
```

#### Remove
```
client.remove("example")
```

## Config Sample
```yaml
{
   uri: "redis://u:password@localhost:6379"
   host: "localhost"
   port: 6379
   password: "password"
   timeout: 2000
   database: 0
}
```