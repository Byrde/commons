package org.byrde.uri

import java.net.URL

import org.byrde.uri.Path.context

case class Queries(queries: Set[(String, String)]) extends AnyVal {
  def ?(query: (String, String)): Queries =
    &(query)

  def &(query: (String, String)): Queries =
    copy(queries = queries + query)

  def ?+(_queries: Set[(String, String)]): Queries =
    &+(_queries)

  def &+(_queries: Set[(String, String)]): Queries =
    copy(queries = queries ++ _queries)

  def withQueries(_queries: Queries): Queries =
    this.copy(queries ++ _queries.queries)

  override def toString: String = {
    queries.foldLeft("") {
      case (acc, param) =>
        acc + {
          if (acc.isEmpty)
            s"?${param._1}=${param._2}"
          else
            s"&${param._1}=${param._2}"
        }
    }
  }.trim
}

object Queries {
  val empty: Queries =
    Queries(Set.empty[(String, String)])

  def fromString(value: String): Queries =
    fromURL(new URL(context, value))

  def fromURL: URL => Queries = {
    case url if Option(url.getQuery).nonEmpty =>
      val queries =
        url
          .getQuery
          .split("&")
          .map { arr =>
            val keyValue =
              arr.split("=")

            keyValue(0) -> keyValue(1)
          }
          .toSet

      Queries(queries)

    case _ =>
      Queries.empty
  }
}