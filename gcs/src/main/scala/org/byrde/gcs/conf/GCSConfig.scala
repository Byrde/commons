package org.byrde.gcs.conf

import org.byrde.gcs.conf.GCSConfig.Bucket

import com.typesafe.config.Config

trait GCSConfig {

  def bucket: Bucket

  def chunk: Int

}

object GCSConfig {

  type Bucket = String

  def apply(config: Config): GCSConfig =
    apply("bucket", "chunk", config)

  def apply(
    _bucket: String,
    _chunk: String,
    config: Config
  ): GCSConfig =
    new GCSConfig {
      override def bucket: Bucket = config.getString(_bucket)

      override def chunk: Int = config.getBytes(_chunk).toInt
    }

}