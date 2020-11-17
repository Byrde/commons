package org.byrde.tapir.support

import akka.http.javadsl.common.JsonEntityStreamingSupport
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling._
import akka.http.scaladsl.util.FastFuture
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString

import io.circe.parser.parse
import io.circe._

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.util.control.NonFatal

trait CirceSupport {
  type SourceOf[A] = Source[A, _]
  
  def unmarshallerContentTypes: Seq[ContentTypeRange] = mediaTypes.map(ContentTypeRange.apply)
  
  def mediaTypes: Seq[MediaType.WithFixedCharset] = List(`application/json`)
  
  private def sourceByteStringMarshaller(mediaType: MediaType.WithFixedCharset): Marshaller[SourceOf[ByteString], MessageEntity] = Marshaller[SourceOf[ByteString], MessageEntity] { _ =>
    value =>
      try FastFuture.successful {
        Marshalling.WithFixedContentType(mediaType, () => HttpEntity(contentType = mediaType, data = value)) :: Nil
      } catch {
        case NonFatal(e) => FastFuture.failed(e)
      }
  }
  
  private val jsonSourceStringMarshaller = Marshaller.oneOf(mediaTypes: _*)(sourceByteStringMarshaller)
  
  private def jsonSource[A](entitySource: SourceOf[A])(implicit encoder: Encoder[A], printer: Printer, support: JsonEntityStreamingSupport): SourceOf[ByteString] = entitySource.map(encoder.apply).map(printer.printToByteBuffer).map(ByteString(_)).via(support.framingRenderer)
  
  /**
   * `ByteString` => `A`
   *
   * @tparam A type to decode
   * @return unmarshaller for any `A` value */
  implicit final def fromByteStringUnmarshaller[A: Decoder]: Unmarshaller[ByteString, A] = byteStringJsonUnmarshaller.map(Decoder[A].decodeJson).map(_.fold(throw _, identity))
  
  /**
   * `Json` => HTTP entity
   *
   * @return marshaller for JSON value */
  implicit final def jsonMarshaller(implicit printer: Printer = Printer.noSpaces): ToEntityMarshaller[Json] = Marshaller.oneOf(mediaTypes: _*) { mediaType =>
    Marshaller.withFixedContentType(ContentType(mediaType)) { json =>
      HttpEntity(mediaType, ByteString(printer.printToByteBuffer(json, mediaType.charset.nioCharset())))
    }
  }
  
  /**
   * `A` => HTTP entity
   *
   * @tparam A type to encode
   * @return marshaller for any `A` value */
  implicit final def marshaller[A: Encoder](implicit printer: Printer = Printer.noSpaces): ToEntityMarshaller[A] = jsonMarshaller(printer).compose(Encoder[A].apply)
  
  /**
   * HTTP entity => `Json`
   *
   * @return unmarshaller for `Json` */
  implicit final val jsonUnmarshaller: FromEntityUnmarshaller[Json] = Unmarshaller.byteStringUnmarshaller.forContentTypes(unmarshallerContentTypes: _*).map { case ByteString.empty => throw Unmarshaller.NoContentException
  case data => jawn.parseByteBuffer(data.asByteBuffer).fold(throw _, identity)
  }
  
  /**
   * HTTP entity => `Either[io.circe.ParsingFailure, Json]`
   *
   * @return unmarshaller for `Either[io.circe.ParsingFailure, Json]` */
  implicit final val safeJsonUnmarshaller: FromEntityUnmarshaller[Either[io.circe.ParsingFailure, Json]] = Unmarshaller.stringUnmarshaller.forContentTypes(unmarshallerContentTypes: _*).map(parse)
  
  /**
   * HTTP entity => `A`
   *
   * @tparam A type to decode
   * @return unmarshaller for `A` */
  implicit final def unmarshaller[A: Decoder]: FromEntityUnmarshaller[A] = jsonUnmarshaller.map(Decoder[A].decodeJson).map(_.fold(throw _, identity))
  
  def byteStringJsonUnmarshaller: Unmarshaller[ByteString, Json] = Unmarshaller(_ => bs => Future.fromTry(jawn.parseByteBuffer(bs.asByteBuffer).toTry))
  
  /**
   * HTTP entity => `Source[A, _]`
   *
   * @tparam A type to decode
   * @return unmarshaller for `Source[A, _]` */
  implicit def sourceUnmarshaller[A: Decoder](implicit support: JsonEntityStreamingSupport = EntityStreamingSupport.json()): FromEntityUnmarshaller[SourceOf[A]] = Unmarshaller.withMaterializer[HttpEntity, SourceOf[A]] { implicit ec =>
    implicit mat =>entity =>
      def asyncParse(bs: ByteString) = Unmarshal(bs).to[A]
      
      def ordered = Flow[ByteString].mapAsync(support.parallelism)(asyncParse)
      
      def unordered = Flow[ByteString].mapAsyncUnordered(support.parallelism)(asyncParse)
      
      Future.successful {
        entity.dataBytes.via(support.framingDecoder).via(if (support.unordered) unordered else ordered)
      }
  }.forContentTypes(unmarshallerContentTypes: _*)
  
  /**
   * `SourceOf[A]` => HTTP entity
   *
   * @tparam A type to encode
   * @return marshaller for any `SourceOf[A]` value */
  implicit def sourceMarshaller[A](implicit writes: Encoder[A], printer: Printer = Printer.noSpaces, support: JsonEntityStreamingSupport = EntityStreamingSupport.json()): ToEntityMarshaller[SourceOf[A]] = jsonSourceStringMarshaller.compose(jsonSource[A])
  
  implicit final def safeUnmarshaller[A: Decoder]: FromEntityUnmarshaller[Either[io.circe.Error, A]] = safeJsonUnmarshaller.map(_.flatMap(Decoder[A].decodeJson))
}
