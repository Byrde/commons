package org.byrde.commons.utils.aws.conf

import com.amazonaws.auth.BasicAWSCredentials

import play.api.Configuration

case class S3Config(bucketName: String,
                    private val awsAccessKey: String,
                    private val awsSecretKey: String) {
  lazy val awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey)
}

object S3Config {
  def apply(config: Configuration): S3Config =
    apply("bucket", "access-key", "secret-key", config)

  def apply(_bucket: String,
            _accessKey: String,
            _secretKey: String,
            config: Configuration): S3Config = {
    val bucket =
      config
        .get[String](_bucket)
    val key =
      config
        .get[String](_accessKey)
    val secretKey =
      config
        .get[String](_secretKey)

    new S3Config(bucket, key, secretKey)
  }
}
