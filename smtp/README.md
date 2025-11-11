# SMTP

Email client abstraction with JavaMail implementation.

## Installation

```scala
resolvers += "GitHub Package Registry" at "https://maven.pkg.github.com/Byrde/commons"

libraryDependencies += "org.byrde" %% "smtp" % "VERSION"
```

## Usage

```scala
import org.byrde.smtp._

// Configure SMTP client
val config = SmtpConfig(
  host = "smtp.gmail.com",
  port = 587,
  username = "your-email@gmail.com",
  password = "your-password",
  connectionType = SmtpConnectionType.TLS
)

val client: EmailClient = new JavaMailClient(config)

// Send email
val request = SmtpRequest(
  to = Seq("recipient@example.com"),
  from = "sender@example.com",
  subject = "Hello",
  body = "Email body content",
  isHtml = false
)

client.send(request) match {
  case Right(_) => println("Email sent successfully")
  case Left(error) => println(s"Failed to send email: $error")
}

// Use test client for development
val testClient = new TestEmailClient()
```

