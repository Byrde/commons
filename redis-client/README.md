# Redis Client

Type-safe Redis client abstraction with Jedis implementation.

## Installation

```scala
resolvers += "GitHub Package Registry" at "https://maven.pkg.github.com/Byrde/commons"

libraryDependencies += "org.byrde" %% "redis-client" % "VERSION"
```

## Usage

```scala
import org.byrde.client.redis._
import org.byrde.client.redis.jedis._
import io.circe.generic.auto._
import scala.concurrent.duration._

// Configure Redis client
val config = JedisConfig(
  host = "localhost",
  port = 6379,
  password = None,
  database = 0
)

val client: RedisClient = new JedisClient(config)

// Type-safe operations with automatic serialization
case class User(id: String, name: String, email: String)

// Store a value with TTL
client.set(
  key = "user:123",
  value = User("123", "John Doe", "john@example.com"),
  expiration = 1.hour
) match {
  case Right(_) => println("User stored")
  case Left(error) => println(s"Error: $error")
}

// Retrieve a value
client.get[User]("user:123") match {
  case Right(Some(redisObject)) =>
    println(s"User: ${redisObject.value}")
    println(s"TTL: ${redisObject.ttl}")
  case Right(None) => println("User not found")
  case Left(error) => println(s"Error: $error")
}

// Remove a value
client.remove("user:123")

// Use custom namespacing
client.set(
  key = "123",
  value = user,
  withNamespace = key => s"users::$key"
)

// Low-level RedisService for string operations
val service: RedisService = new JedisService(config)

service.get("raw:key") match {
  case Right(Some(value)) => println(value)
  case Right(None) => println("Key not found")
  case Left(error) => println(s"Error: $error")
}

// Use in-memory implementation for testing
val testClient = new InMemoryRedisClient()
val testService = new InMemoryRedisService()
```

