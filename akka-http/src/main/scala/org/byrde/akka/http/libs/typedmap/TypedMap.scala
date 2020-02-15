package org.byrde.akka.http.libs.typedmap

import scala.collection.immutable

/**
 * A TypedMap is an immutable map containing typed values. Each entry is
 * associated with a [[TypedKey]] that can be used to look up the value. A
 * `TypedKey` also defines the type of the value, e.g. a `TypedKey[String]`
 * would be associated with a `String` value.
 *
 * Instances of this class are created with the `TypedMap.empty` method.
 *
 * The elements inside TypedMaps cannot be enumerated. This is a decision
 * designed to enforce modularity. It's not possible to accidentally or
 * intentionally access a value in a TypedMap without holding the
 * corresponding [[TypedKey]].
 */
final class TypedMap(val m: immutable.Map[TypedKey[_], Any]) {

  /**
   * Get a value from the map, throwing an exception if it is not present.
   *
   * @param key The key for the value to retrieve.
   * @tparam A The type of value to retrieve.
   * @return The value, if it is present in the map.
   * @throws scala.NoSuchElementException If the value isn't present in the map.
   */
  def apply[A](key: TypedKey[A]): A =
    m.apply(key).asInstanceOf[A]

  /**
   * Get a value from the map, returning `None` if it is not present.
   *
   * @param key The key for the value to retrieve.
   * @tparam A The type of value to retrieve.
   * @return `Some` value, if it is present in the map, otherwise `None`.
   */
  def get[A](key: TypedKey[A]): Option[A] =
    m.get(key).asInstanceOf[Option[A]]

  /**
   * Check if the map contains a value with the given key.
   *
   * @param key The key to check for.
   * @return True if the value is present, false otherwise.
   */
  def contains(key: TypedKey[_]): Boolean =
    m.contains(key)

  /**
   * Update the map with the given key and value, returning a new instance of the map.
   *
   * @param key The key to set.
   * @param value The value to use.
   * @tparam A The type of value.
   * @return A new instance of the map with the new entry added.
   */
  def updated[A](key: TypedKey[A], value: A): TypedMap =
    new TypedMap(m.updated(key, value))

  /**
   * Update the map with several entries, returning a new instance of the map.
   *
   * @param entries The new entries to add to the map.
   * @return A new instance of the map with the new entries added.
   */
  def +(entries: TypedEntry[_]*): TypedMap = {
    val m2 =
      entries.foldLeft(m) {
        case (m1, e) =>
          m1.updated(e.key, e.value)
      }

    new TypedMap(m2)
  }

  override def toString: String =
    m.mkString("{", ", ", "}")

}

object TypedMap {

  /**
   * The empty [[TypedMap]] instance.
   */
  val empty = new TypedMap(immutable.Map.empty)

  /**
   * Builds a [[TypedMap]] from a list of keys and values.
   */
  def apply(entries: TypedEntry[_]*): TypedMap =
    TypedMap.empty.+(entries: _*)

}