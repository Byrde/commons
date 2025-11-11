# Commons

Core utilities and type definitions for Scala projects.

## Installation

```scala
resolvers += "GitHub Package Registry" at "https://maven.pkg.github.com/Byrde/commons"

libraryDependencies += "org.byrde" %% "commons" % "VERSION"
```

## Usage

```scala
import org.byrde.commons._

// Implicit conversions for cleaner code
val futureValue = someValue.f
val optionValue = someValue.?
val eitherValue = someValue.r
val tryValue = someValue.!+

// Domain types
val email = Email("user@example.com")
val phone = Phone("+1234567890")
val money = Money(100.50, "USD")

// JWT handling
val jwt = Jwt.encode(payload, secret)
Jwt.decode(token, secret) match {
  case Right(claims) => // Valid token
  case Left(error) => // Invalid token
}

// URI building
val url = Url(
  protocol = Protocol.Https,
  host = Host("api.example.com"),
  port = Port(443),
  path = Path("/v1/users"),
  queries = Queries("limit" -> "10")
)
```

