package org.byrde.akka.http.rejections

import akka.http.scaladsl.server.Rejection

import scala.util.control.NoStackTrace

class RejectionException extends NoStackTrace with Rejection
