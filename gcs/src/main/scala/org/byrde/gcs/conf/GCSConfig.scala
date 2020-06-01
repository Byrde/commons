package org.byrde.gcs.conf

import org.byrde.gcs.conf.GCSConfig.Bucket

import com.typesafe.config.Config

case class GCSConfig(
  bucket: Bucket,
  chunk: Int,
)

object GCSConfig {

  type Bucket = String

  def apply(config: Config): GCSConfig =
    apply("bucket", "chunk", config)

  def apply(
    _bucket: String,
    _chunk: String,
    config: Config
  ): GCSConfig =
    GCSConfig(
      config.getString(_bucket),
      config.getBytes(_chunk).toInt
    )

}