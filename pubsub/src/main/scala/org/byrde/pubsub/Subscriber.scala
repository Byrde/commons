package org.byrde.pubsub

import com.google.api.gax.core.CredentialsProvider
import com.google.auth.Credentials
import com.google.cloud.pubsub.v1.MessageReceiver

import org.byrde.logging.Logger

import io.circe.Decoder
import io.circe.generic.auto._
import io.circe.parser.parse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util._
import scala.util.chaining._

trait Subscriber {
  def subscribe[T](
    credentials: Credentials,
    subscription: String,
  )(fn: Envelope[T] => Future[_])(implicit logger: Logger, decoder: Decoder[T]): Future[Unit] =
    receiver(fn)
      .pipe(com.google.cloud.pubsub.v1.Subscriber.newBuilder(subscription, _))
      .setCredentialsProvider {
        new CredentialsProvider {
          override def getCredentials: Credentials = credentials
        }
      }
      .build()
      .pipe { sub =>
        logger.logInfo(s"Starting subscriber $subscription!")
        Future(sub.awaitRunning())
          .recoverWith {
            case ex =>
              logger.logError("Error starting subscriber!", ex)
              Future(sub.awaitTerminated()).flatMap(_ => Future.failed(ex))
          }
      }
  
  private def receiver[T](
    fn: Envelope[T] => Future[_],
  )(implicit logger: Logger, decoder: Decoder[T]): MessageReceiver = {
    case (message, consumer) =>
    parse(message.getData.toStringUtf8)
      .flatMap(_.as[Envelope[T]])
      .left
      .map(PubSubError.DecodingError.apply(s"Error decoding message: ${message}")(_))
      .pipe {
        case Right(value) =>
          fn(value)

        case Left(exception) =>
          Future.failed(exception)
      }
      .map(_ => consumer.ack())
      .recover {
        case ex =>
          logger.logError("Error processing PubSubMessage!", ex)
          consumer.nack()
          ()
      }
  }
}

object Subscriber extends Subscriber