package org.byrde.commons.models.uri

import java.net.URL

case class Path(path: Seq[String], queries: Queries = Queries.empty) {
  def /(newPath: String): Path =
    copy(path = path :+ newPath)

  def /(newPath: Path): Path =
    copy(path = path ++ newPath.path, queries = queries withQueries newPath.queries)

  def +(newQuery: (String, String)): Path =
    copy(queries = queries + newQuery)

  def ++(newQuery: Set[(String, String)]): Path =
    copy(queries = queries ++ newQuery)

  def withQueries(newQuery: Queries): Path =
    copy(queries = queries withQueries newQuery)

  override def toString: String =
    (if (path.nonEmpty) path.mkString("/") else "").trim + queries.toString
}

object Path {
  val context: URL =
    new URL("http", "na.com", "")

  def empty(queries: Queries = Queries.empty): Path =
    Path(Nil, queries)

  def apply(value: String): Path =
    Path(value :: Nil)

  def fromString(value: String): Path =
    if (value.isEmpty)
      Path.empty()
    else
      fromURL(new URL(context, value))

  def fromURL: URL => Path = {
    case url if Option(url.getPath).nonEmpty && url.getPath.head == '/' =>
      val originalPath =
        url.getPath.split("/")

      val startOpt =
        originalPath
          .headOption
          .map(start => Path.apply(Seq(start)))

      val path =
        startOpt
          .map { start =>
            originalPath
              .drop(1)
              .foldLeft(start)(_ / _)
          }

      val queries =
        Queries.fromURL(url)

      path.fold(empty(queries))(_.copy(queries = queries))

    case url =>
      Path.empty(Queries.fromURL(url))
  }
}