# Logging

Scala logging abstractions built on scala-logging.

## Installation

```scala
resolvers += "GitHub Package Registry" at "https://maven.pkg.github.com/Byrde/commons"

libraryDependencies += "org.byrde" %% "logging" % "VERSION"
```

## Usage

```scala
import org.byrde.logging._

// Extend ScalaLogger trait
class MyService extends ScalaLogger {
  def processRequest(id: String): Unit = {
    logger.info(s"Processing request: $id")
    
    try {
      // Business logic
      logger.debug("Request processed successfully")
    } catch {
      case ex: Exception =>
        logger.error(s"Failed to process request $id", ex)
    }
  }
}

// Or use Logger directly
object MyApp {
  private val logger = Logger(getClass)
  
  def main(args: Array[String]): Unit = {
    logger.info("Application started")
    logger.warn("This is a warning")
  }
}
```

