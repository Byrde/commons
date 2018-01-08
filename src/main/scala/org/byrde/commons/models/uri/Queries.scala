package org.byrde.commons.models.uri

case class Queries(queries: Set[(String, String)]) extends AnyVal {
  def +(newQuery: (String, String)): Queries =
    this.copy(queries + newQuery)

  def ++(newQuery: Set[(String, String)]): Queries =
    this.copy(queries ++ newQuery)

  override def toString: String = {
    val query = queries.foldLeft("?")(
      (acc, param) =>
        acc + {
          if (acc == "?") s"${param._1}=${param._2}"
          else s"&${param._1}=${param._2}"
      }
    )
    if (query != "?") query else ""
  }
}
