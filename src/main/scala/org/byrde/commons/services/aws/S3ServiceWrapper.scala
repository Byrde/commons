package org.byrde.commons.services.aws

import java.io._

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3ObjectInputStream

import org.byrde.commons.models.services.ServiceResponse
import org.byrde.commons.models.services.CommonsServiceResponseDictionary.E0200
import org.byrde.commons.utils.aws.conf.S3Config
import org.byrde.commons.utils.compressor.conf.{
  HtmlCompressorConfig,
  JsCompressorConfig
}

import scala.concurrent.{ExecutionContext, Future}

case class S3ServiceWrapper(s3Config: S3Config,
                            _htmlConfig: Option[HtmlCompressorConfig] = None,
                            _jsConfig: Option[JsCompressorConfig] = None)(
    implicit ec: ExecutionContext) {
  private def client =
    new AmazonS3Client(s3Config.awsCredentials)

  def storeFile(
      file: File,
      fileName: Option[String] = None): Future[ServiceResponse[String]] = {
    if (!client.doesBucketExist(s3Config.bucketName))
      client.createBucket(s3Config.bucketName)

    Future(
      client.putObject(s3Config.bucketName,
                       fileName.getOrElse(file.getName),
                       file)).map { _ =>
      E0200.withMessage("File uploaded successfully")
    }
  }

  def retrieveFile(fileName: String): Future[S3ObjectInputStream] =
    Future(client.getObject(s3Config.bucketName, fileName))
      .map(_.getObjectContent)

  def doesFileExist(fileName: String) =
    Future(client.doesObjectExist(s3Config.bucketName, fileName))
}
