# Refactoring Examples - Interface Design Improvements

This document provides concrete examples of how to refactor modules to follow clean, testable interface patterns.

---

## 1. PubSub Module Refactoring

### Current Design Issues

The current `Publisher` and `Subscriber` are abstract classes with:
- Required constructor parameters (logger)
- Internal mutable state (TrieMap)
- No trait interface for dependency injection
- Tightly coupled to Google Pub/Sub API

### Proposed Design

#### Step 1: Define Trait Interfaces

```scala
package org.byrde.pubsub

import scala.concurrent.Future

trait MessagePublisher[T] {
  def publish(envelope: Envelope[T]): Future[Either[PubSubError, Unit]]
  def close(): Unit
}

trait MessageSubscriber[T] {
  def subscribe(
    subscription: String,
    topic: String,
  )(fn: Envelope[T] => Future[Either[Nack.type, Ack.type]]): Future[Either[PubSubError, Unit]]
  def close(): Unit
}
```

#### Step 2: Create Configuration

```scala
package org.byrde.pubsub.conf

import com.google.auth.Credentials

case class PubSubConfig(
  project: String,
  credentials: Credentials,
  hostOpt: Option[String] = None,
  enableExactlyOnceDelivery: Boolean = false,
  enableMessageOrdering: Boolean = false,
)

object PubSubConfig {
  def apply(config: com.typesafe.config.Config): PubSubConfig = ???
}
```

#### Step 3: Implement Google Pub/Sub Concrete Classes

```scala
package org.byrde.pubsub.google

import org.byrde.pubsub._
import org.byrde.pubsub.conf.PubSubConfig
import org.byrde.logging.Logger

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.collection.concurrent.TrieMap

class GooglePubSubPublisher(
  config: PubSubConfig,
  logger: Logger,
)(implicit ec: ExecutionContextExecutor)
  extends MessagePublisher[T]
  with AutoCloseable {

  private type Topic = String
  private val publishers: TrieMap[Topic, com.google.cloud.pubsub.v1.Publisher] = TrieMap.empty

  override def publish(envelope: Envelope[T]): Future[Either[PubSubError, Unit]] = {
    // Implementation here (similar to current Publisher)
    ???
  }

  override def close(): Unit = {
    logger.logInfo("Shutting down all publishers...")
    publishers.foreach { case (topic, publisher) =>
      publishers.remove(topic)
      publisher.shutdown()
    }
  }
}

class GooglePubSubSubscriber(
  config: PubSubConfig,
  logger: Logger,
)(implicit ec: ExecutionContextExecutor)
  extends MessageSubscriber[T]
  with AutoCloseable {

  private type Subscription = String
  private val subscribers: TrieMap[Subscription, com.google.cloud.pubsub.v1.Subscriber] = TrieMap.empty

  override def subscribe(
    subscription: String,
    topic: String,
  )(fn: Envelope[T] => Future[Either[Nack.type, Ack.type]]): Future[Either[PubSubError, Unit]] = {
    // Implementation here (similar to current Subscriber)
    ???
  }

  override def close(): Unit = {
    logger.logInfo("Shutting down all subscribers...")
    subscribers.foreach { case (subscription, subscriber) =>
      subscribers.remove(subscription)
      subscriber.stopAsync()
    }
  }
}
```

#### Step 4: Create Test Implementation

```scala
package org.byrde.pubsub.test

import org.byrde.pubsub._

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

class InMemoryPublisher[T] extends MessagePublisher[T] {
  val published: TrieMap[String, List[Envelope[T]]] = TrieMap.empty

  override def publish(envelope: Envelope[T]): Future[Either[PubSubError, Unit]] = {
    published.updateWith(envelope.topic) {
      case Some(envelopes) => Some(envelopes :+ envelope)
      case None => Some(List(envelope))
    }
    Future.successful(Right(()))
  }

  override def close(): Unit = published.clear()

  def getPublished(topic: String): List[Envelope[T]] = published.getOrElse(topic, List.empty)
}

class InMemorySubscriber[T] extends MessageSubscriber[T] {
  private val handlers: TrieMap[String, Envelope[T] => Future[Either[Nack.type, Ack.type]]] = TrieMap.empty

  override def subscribe(
    subscription: String,
    topic: String,
  )(fn: Envelope[T] => Future[Either[Nack.type, Ack.type]]): Future[Either[PubSubError, Unit]] = {
    handlers.put(subscription, fn)
    Future.successful(Right(()))
  }

  override def close(): Unit = handlers.clear()

  // Test helper to simulate message delivery
  def simulateMessage(subscription: String, envelope: Envelope[T]): Future[Either[Nack.type, Ack.type]] = {
    handlers.get(subscription) match {
      case Some(handler) => handler(envelope)
      case None => Future.successful(Left(Nack))
    }
  }
}
```

#### Step 5: Usage Example

```scala
// Production code
val config = PubSubConfig(
  project = "my-project",
  credentials = myCredentials,
)

val publisher: MessagePublisher[MyMessage] = new GooglePubSubPublisher(config, logger)

// Easy to test
class MyService(publisher: MessagePublisher[MyMessage]) {
  def sendNotification(msg: MyMessage): Future[Either[PubSubError, Unit]] = {
    publisher.publish(Envelope(topic = "notifications", msg = msg))
  }
}

// Test code
val testPublisher = new InMemoryPublisher[MyMessage]
val service = new MyService(testPublisher)

service.sendNotification(myMessage)
assert(testPublisher.getPublished("notifications").size == 1)
```

#### Benefits

1. ✅ Trait interface allows dependency injection
2. ✅ Easy to create test implementations
3. ✅ Concrete implementation isolated from interface
4. ✅ No abstract class inheritance required
5. ✅ Configuration externalized
6. ✅ Proper error handling with Either

---

## 2. SMTP Module Refactoring

### Current Design Issues

- Concrete class only, no trait interface
- Throws exceptions instead of using Either
- Tightly coupled to JavaMail
- Hard to test

### Proposed Design

#### Step 1: Define Trait Interface

```scala
package org.byrde.smtp

import org.byrde.commons.types.Email

sealed trait SmtpError extends Throwable
object SmtpError {
  case class SendFailure(cause: Throwable) extends SmtpError
  case class InvalidConfiguration(message: String) extends SmtpError
  case class InvalidRecipient(email: String) extends SmtpError
}

trait EmailClient {
  def send(request: SmtpRequest): Either[SmtpError, Unit]
}
```

#### Step 2: Refactor Concrete Implementation

```scala
package org.byrde.smtp.impl

import org.byrde.smtp._
import org.byrde.smtp.conf.SmtpConfig
import org.byrde.commons.types.Email

import javax.mail.internet._
import javax.mail.{Message, Transport}
import scala.util.{Try, Success, Failure}
import scala.util.chaining._
import org.jsoup.Jsoup

class JavaMailClient(config: SmtpConfig) extends EmailClient {
  
  override def send(request: SmtpRequest): Either[SmtpError, Unit] = {
    Try {
      val message = buildEmail(request)
      Transport.send(message, config.user, config.password)
    } match {
      case Success(_) => Right(())
      case Failure(ex) => Left(SmtpError.SendFailure(ex))
    }
  }

  private def buildEmail(request: SmtpRequest): MimeMessage =
    buildEmail(request.recipient, request.subject)(buildBody(request))

  private def buildEmail(recipient: Email, subject: String)(mimeMultipart: MimeMultipart): MimeMessage =
    new MimeMessage(config.session)
      .tap(_.setContent(mimeMultipart))
      .tap(_.setFrom(new InternetAddress(config.from.toString)))
      .tap(_.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient.toString)))
      .tap(_.setSubject(subject))

  private def buildBody(request: SmtpRequest): MimeMultipart =
    new MimeMultipart("alternative")
      .tap(_.addBodyPart {
        new MimeBodyPart().tap(_.setText(Jsoup.parse(request.htmlContent).body.text, "utf-8"))
      })
      .tap(_.addBodyPart {
        new MimeBodyPart().tap(_.setText(request.htmlContent, "utf-8", "html"))
      })
}
```

#### Step 3: Create Test Implementation

```scala
package org.byrde.smtp.test

import org.byrde.smtp._

import scala.collection.mutable.ListBuffer

class TestEmailClient extends EmailClient {
  private val _sent: ListBuffer[SmtpRequest] = ListBuffer.empty
  private var _shouldFail: Option[SmtpError] = None

  override def send(request: SmtpRequest): Either[SmtpError, Unit] = {
    _shouldFail match {
      case Some(error) => Left(error)
      case None =>
        _sent += request
        Right(())
    }
  }

  // Test helpers
  def sentEmails: List[SmtpRequest] = _sent.toList
  def setShouldFail(error: SmtpError): Unit = _shouldFail = Some(error)
  def reset(): Unit = {
    _sent.clear()
    _shouldFail = None
  }
}
```

#### Step 4: Usage Example

```scala
// Production code
val config = SmtpConfig(
  host = "smtp.gmail.com",
  user = "api",
  password = "secret",
  from = Email.fromString("noreply@example.com").getOrElse(throw new Exception("Invalid email")),
  `type` = SmtpConnectionType.TLS,
)

val emailClient: EmailClient = new JavaMailClient(config)

// Service using the client
class NotificationService(emailClient: EmailClient) {
  def sendWelcomeEmail(recipient: Email): Either[SmtpError, Unit] = {
    val request = SmtpRequest(
      recipient = recipient,
      subject = "Welcome!",
      htmlContent = "<h1>Welcome to our service!</h1>",
    )
    emailClient.send(request)
  }
}

// Test code
class NotificationServiceSpec extends ... {
  test("sendWelcomeEmail sends email with correct recipient") {
    val testClient = new TestEmailClient
    val service = new NotificationService(testClient)
    
    val recipient = Email.fromString("user@example.com").getOrElse(fail("Invalid email"))
    service.sendWelcomeEmail(recipient)
    
    assert(testClient.sentEmails.size == 1)
    assert(testClient.sentEmails.head.recipient == recipient)
    assert(testClient.sentEmails.head.subject == "Welcome!")
  }
  
  test("sendWelcomeEmail handles send failures") {
    val testClient = new TestEmailClient
    testClient.setShouldFail(SmtpError.SendFailure(new Exception("Network error")))
    val service = new NotificationService(testClient)
    
    val recipient = Email.fromString("user@example.com").getOrElse(fail("Invalid email"))
    val result = service.sendWelcomeEmail(recipient)
    
    assert(result.isLeft)
  }
}
```

#### Benefits

1. ✅ Trait interface allows dependency injection
2. ✅ Easy to create test implementations
3. ✅ Proper error handling with Either
4. ✅ No exceptions in normal flow
5. ✅ Testable without network calls
6. ✅ Clear error types

---

## 3. Configuration Pattern Standardization

### Recommended Pattern for All Modules

```scala
package org.byrde.module.conf

import com.typesafe.config.Config
import scala.util.Try

// Define configuration as case class
case class ModuleConfig(
  host: String,
  port: Int,
  timeout: Int,
  // ... other config params
)

object ModuleConfig {
  // Default values
  val DefaultHost = "localhost"
  val DefaultPort = 8080
  val DefaultTimeout = 2000

  /** Configuration example:
    * {{{
    * module {
    *   host: "localhost"
    *   port: 8080
    *   timeout: 2000
    * }
    * }}}
    */
  def apply(config: Config): ModuleConfig = {
    val host = Try(config.getString("host")).getOrElse(DefaultHost)
    val port = Try(config.getInt("port")).getOrElse(DefaultPort)
    val timeout = Try(config.getInt("timeout")).getOrElse(DefaultTimeout)
    
    ModuleConfig(host, port, timeout)
  }
  
  // Alternative constructor with validation
  def fromConfig(config: Config): Either[ConfigError, ModuleConfig] = {
    Try {
      val host = config.getString("host")
      val port = config.getInt("port")
      val timeout = config.getInt("timeout")
      
      if (port < 1 || port > 65535)
        throw new IllegalArgumentException(s"Invalid port: $port")
      
      ModuleConfig(host, port, timeout)
    }.toEither.left.map(ex => ConfigError.Invalid(ex.getMessage))
  }
}

sealed trait ConfigError
object ConfigError {
  case class Invalid(message: String) extends ConfigError
  case class Missing(key: String) extends ConfigError
}
```

---

## 4. Redis Serialization Refactoring

### Current Design Issues

- Serialization logic embedded in `RedisClient`
- String-based type detection is fragile
- Hard to test serialization separately

### Proposed Design

#### Step 1: Define Serialization Interface

```scala
package org.byrde.client.redis

import scala.util.Try

trait RedisSerializer[T] {
  def serialize(value: T): Either[SerializationError, String]
  def deserialize(data: String): Either[SerializationError, T]
}

sealed trait SerializationError extends Throwable
object SerializationError {
  case class EncodingFailure(cause: Throwable) extends SerializationError
  case class DecodingFailure(cause: Throwable) extends SerializationError
  case class UnknownType(typePrefix: String) extends SerializationError
}
```

#### Step 2: Implement Type-Safe Serialization

```scala
package org.byrde.client.redis.serialization

import org.byrde.client.redis._
import io.circe.{Encoder, Decoder, Printer}
import io.circe.parser._
import io.circe.syntax._

import java.io._
import java.util.Base64
import scala.util.{Try, Using}

sealed trait SerializationType
object SerializationType {
  case object Json extends SerializationType
  case object String extends SerializationType
  case object Int extends SerializationType
  case object Long extends SerializationType
  case object Boolean extends SerializationType
}

class CirceRedisSerializer[T](implicit encoder: Encoder[T], decoder: Decoder[T])
  extends RedisSerializer[T] {
  
  private val printer: Printer = Printer.noSpaces.copy(dropNullValues = true)

  override def serialize(value: T): Either[SerializationError, String] = {
    val (typePrefix, data) = serializeValue(value)
    Try {
      val encoded = Base64.getEncoder.encodeToString(data)
      s"$typePrefix-$encoded"
    }.toEither.left.map(ex => SerializationError.EncodingFailure(ex))
  }

  override def deserialize(data: String): Either[SerializationError, T] = {
    val parts = data.split("-", 2)
    if (parts.length != 2) {
      return Left(SerializationError.DecodingFailure(
        new IllegalArgumentException("Invalid format")
      ))
    }

    val (typePrefix, encodedData) = (parts(0), parts(1))
    Try {
      Base64.getDecoder.decode(encodedData)
    }.toEither.left.map(SerializationError.DecodingFailure.apply)
      .flatMap(bytes => deserializeValue(typePrefix, bytes))
  }

  private def serializeValue(value: T): (String, Array[Byte]) = {
    value match {
      case v: String =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeUTF(v)
        ("string", baos.toByteArray)

      case v: Int =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeInt(v)
        ("int", baos.toByteArray)

      case v: Long =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeLong(v)
        ("long", baos.toByteArray)

      case v: Boolean =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeBoolean(v)
        ("boolean", baos.toByteArray)

      case v =>
        val baos = new ByteArrayOutputStream()
        val dos = new DataOutputStream(baos)
        dos.writeUTF(v.asJson.printWith(printer))
        ("json", baos.toByteArray)
    }
  }

  private def deserializeValue(typePrefix: String, bytes: Array[Byte]): Either[SerializationError, T] = {
    typePrefix match {
      case "json" =>
        Using(new DataInputStream(new ByteArrayInputStream(bytes))) { dis =>
          dis.readUTF()
        }.toEither
          .left.map(SerializationError.DecodingFailure.apply)
          .flatMap(parse(_).left.map(SerializationError.DecodingFailure.apply))
          .flatMap(_.as[T].left.map(SerializationError.DecodingFailure.apply))

      case "string" =>
        deserializePrimitive(bytes)(_.readUTF().asInstanceOf[T])

      case "int" =>
        deserializePrimitive(bytes)(_.readInt().asInstanceOf[T])

      case "long" =>
        deserializePrimitive(bytes)(_.readLong().asInstanceOf[T])

      case "boolean" =>
        deserializePrimitive(bytes)(_.readBoolean().asInstanceOf[T])

      case unknown =>
        Left(SerializationError.UnknownType(unknown))
    }
  }

  private def deserializePrimitive[A](bytes: Array[Byte])(f: DataInputStream => A): Either[SerializationError, A] = {
    Using(new DataInputStream(new ByteArrayInputStream(bytes)))(f)
      .toEither
      .left.map(SerializationError.DecodingFailure.apply)
  }
}
```

#### Benefits

1. ✅ Serialization logic isolated and testable
2. ✅ Type-safe with enum instead of string matching
3. ✅ Proper error handling
4. ✅ Can be tested independently
5. ✅ Can be swapped with different implementations

---

## Summary

These refactorings demonstrate the recommended patterns for creating clean, testable interfaces:

1. **Define trait interfaces** - Abstract the behavior, not the implementation
2. **Implement concrete classes** - Separate interface from implementation
3. **Create test implementations** - Make testing easy
4. **Use Either for errors** - No exceptions in normal flow
5. **Externalize configuration** - Use case classes with companion objects
6. **Dependency injection** - Constructor parameters for dependencies

By following these patterns, the codebase becomes more maintainable, testable, and flexible.

