package org.byrde.uri

import java.net.URL

import org.byrde.uri.Path.context

case class Queries(queries: Set[(String, String)]) extends AnyVal {
  def &+(query: (String, String)): Queries =
    copy(queries = queries + query)
  
  def &+(query: Option[(String, String)]): Queries =
    query.fold(this)(query => copy(queries = queries + query))
  
  def &+(_queries: Queries): Queries =
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
  
  def apply(query: (String, String)): Queries =
    Queries(Set(query))

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