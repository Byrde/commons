# PubSub

Pub/sub messaging abstraction with Google Cloud Pub/Sub implementation.

## Installation

```scala
resolvers += "GitHub Package Registry" at "https://maven.pkg.github.com/Byrde/commons"

libraryDependencies += "org.byrde" %% "pubsub" % "VERSION"
```

## Usage

```scala
import org.byrde.pubsub._
import org.byrde.pubsub.google._
import io.circe.syntax._

// Configure Google Pub/Sub
val config = PubSubConfig(
  projectId = "your-gcp-project",
  credentialsPath = "/path/to/credentials.json"
)

// Publishing messages
val publisher: MessagePublisher = new GooglePubSubPublisher(config)
val message = MyEvent("data").asJson.noSpaces

publisher.publish("my-topic", message) match {
  case Right(messageId) => println(s"Published: $messageId")
  case Left(error) => println(s"Failed to publish: $error")
}

// Subscribing to messages
val subscriber: MessageSubscriber = new GooglePubSubSubscriber(config)

subscriber.subscribe("my-subscription") { envelope =>
  val data = envelope.data
  
  try {
    // Process message
    println(s"Received: $data")
    Ack // Acknowledge successful processing
  } catch {
    case ex: Exception =>
      println(s"Processing failed: ${ex.getMessage}")
      Nack // Negative acknowledgment - message will be redelivered
  }
}

// Use in-memory implementation for testing
val testPublisher = new InMemoryPublisher()
val testSubscriber = new InMemorySubscriber()
```

