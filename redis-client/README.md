# Redis Client [![Latest Version @ Cloudsmith](https://api-prd.cloudsmith.io/badges/version/byrde/libraries/maven/redis-client_2.13/latest/x/?render=true)](https://cloudsmith.io/~byrde/repos/libraries/packages/detail/maven/redis-client_2.13/latest/)

Abstraction for a Zio Redis Client.

## How to install

* add to your dependencies library dependencies:
```libraryDependencies += "org.byrde" %% "redis-client" % "VERSION"```

* add this resolver to your resolvers dependencies:
```resolvers += "byrde-libraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/"```

## Quickstart
This library defines a common interface for implementing a redis client.
Compatible implementations:
- [jedis-client](https://github.com/Byrde/commons/tree/master/jedis-client)

#### Usage
```scala
class MyService() extends RedisService {

  def keys(pattern: String): Future[Either[RedisClientError, Set[String]]] = ???

  def get(key: Key): Future[Either[RedisClientError, Option[String]]] = ???

  def set(key: Key, value: String, expiration: Duration = Duration.Inf): Future[Either[RedisClientError, Unit]] = ???

  def del(key: Key): Future[Either[RedisClientError, Long]] = ???

  def ttl(key: Key): Future[Either[RedisClientError, Long]] = ???

}

class MyClient(env: MyService) extends RedisClient[MyService] {

  def executor: RedisExecutor.Service[MyService] = ???

}

val service = new MyService()

val client = new MyClient()
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