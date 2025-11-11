package org.byrde.smtp.test

import org.byrde.smtp._

import scala.collection.mutable.ListBuffer

/** In-memory implementation of EmailClient for testing.
  *
  * This implementation stores sent emails in memory and provides methods to inspect them for test assertions.
  */
class TestEmailClient extends EmailClient {
  private val _sent: ListBuffer[SmtpRequest] = ListBuffer.empty
  private var _shouldFail: Option[SmtpError] = None

  override def send(request: SmtpRequest): Either[SmtpError, Unit] =
    _shouldFail match {
      case Some(error) =>
        Left(error)

      case None =>
        _sent += request
        Right(())
    }

  // Test helpers

  /** Returns all sent emails.
    */
  def sentEmails: List[SmtpRequest] = _sent.toList

  /** Returns the number of sent emails.
    */
  def sentCount: Int = _sent.size

  /** Returns the last sent email, if any.
    */
  def lastSent: Option[SmtpRequest] = _sent.lastOption

  /** Clears all sent emails.
    */
  def clear(): Unit = _sent.clear()

  /** Sets the client to fail with the given error on the next send.
    */
  def setShouldFail(error: SmtpError): Unit = _shouldFail = Some(error)

  /** Clears the failure setting.
    */
  def clearFailure(): Unit = _shouldFail = None

  /** Resets the client to initial state (clears sent emails and failures).
    */
  def reset(): Unit = {
    clear()
    clearFailure()
  }

  /** Returns emails sent to a specific recipient.
    */
  def sentTo(recipient: String): List[SmtpRequest] = _sent.filter(_.recipient.toString == recipient).toList

  /** Returns emails with a specific subject.
    */
  def withSubject(subject: String): List[SmtpRequest] = _sent.filter(_.subject == subject).toList
}
