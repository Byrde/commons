# Redis Client

Abstraction for a Redis Client.

## How to install

* add to your dependencies library dependencies:
```libraryDependencies += "org.byrde" %% "redis-client" % "VERSION"```

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