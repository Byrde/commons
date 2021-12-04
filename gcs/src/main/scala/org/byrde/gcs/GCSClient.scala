package org.byrde.gcs

import org.byrde.gcs.conf.GCSConfig

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{ContentType, HttpCharset, HttpCharsets, MediaTypes}
import akka.stream.alpakka.googlecloud.storage.scaladsl.GCStorage
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

class GCSClient(config: GCSConfig)(implicit ec: ExecutionContext, system: ActorSystem) {
  def upload(name: String, content: Source[ByteString, _]): Future[Unit] =
    content.runWith(sink(name)).map(_ => ())

  private def sink(name: String) =
    GCStorage.resumableUpload(
      config.bucket,
      name,
      ContentType(
        MediaTypes.forExtension(name),
        () => HttpCharsets.`UTF-8`: HttpCharset
      ),
      config.chunk
    )
}
