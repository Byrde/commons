package org.byrde.akka.http.scaladsl.server.directives

import org.byrde.akka.http.libs.typedmap.{TypedEntry, TypedKey, TypedMap}

import akka.http.scaladsl.model.HttpRequest

/**
 * A wrapper for a processed and enriched [[HttpRequest]]
 *
 * @param body Request body entity
 * @param request Underlying request
 * @param attrs A map of typed attributes associated with the request.
 * @tparam T Request body entity type
 */
class HttpRequestWithEntity[T](val body: T, val request: HttpRequest, attrs: TypedMap = TypedMap.empty) {
  /**
   * Get a value from the map, returning [[None]] if it is not present.
   *
   * @param key The key for the value to retrieve.
   * @tparam A The type of value to retrieve.
   * @return [[Some]] value, if it is present in the map, otherwise [[None]].
   */
  def getAttr[A](key: TypedKey[A]): Option[A] =
    attrs.get(key)

  /**
    * Create a new version of this object with the given attributes attached to it.
    * This replaces any existing attributes.
    *
    * @param newAttr The new attribute to add.
    * @return The new version of this object with the attributes attached.
    */
  def withAttr(newAttr: TypedEntry[_]): HttpRequestWithEntity[T] =
    new HttpRequestWithEntity(body, request, attrs + newAttr)

  /**
   * Create a new version of this object with the given attributes attached to it.
   * This replaces any existing attributes.
   *
   * @param newAttrs The new attributes.
   * @return The new version of this object with the attributes attached.
   */
  def withAttrs(newAttrs: TypedMap): HttpRequestWithEntity[T] =
    new HttpRequestWithEntity(body, request, newAttrs)
}
