package org.byrde.commons.types.uri

import java.net.{URI, URL}

case class Path(path: Seq[String], queries: Queries = Queries.empty) {
  def / (newPath: String): Path = copy(path = path :+ newPath)

  def / (newPath: Path): Path = copy(path = path ++ newPath.path, queries = queries &+ newPath.queries)

  def &+ (query: (String, String)): Path = copy(queries = queries &+ query)

  def &+ (query: Option[(String, String)]): Path = copy(queries = queries &+ query)

  def &+ (_queries: Queries): Path = copy(queries = queries &+ _queries)

  override def toString: String = (if (path.nonEmpty) path.mkString("/", "/", "") else "/").trim + queries.toString
}

object Path {
  val context: URL = URI.create("http://example.com").toURL

  def empty(queries: Queries = Queries.empty): Path = Path(Nil, queries)

  def apply(value: String): Path = Path(value :: Nil)

  def fromString(value: String): Path =
    if (value.isEmpty)
      Path.empty()
    else
      fromURL(context.toURI.resolve(value).toURL)

  def fromURL: URL => Path = {
    case url if Option(url.getPath).nonEmpty && url.getPath.head == '/' =>
      val originalPath = url.getPath.split("/")

      val originalPath2 =
        if (originalPath.size <= 1)
          originalPath
        else
          originalPath.drop(1)

      val startOpt = originalPath2.headOption.map(Path.apply)

      val path =
        startOpt.map { start =>
          originalPath2.drop(1).foldLeft(start)(_ / _)
        }

      val queries = Queries.fromURL(url)

      path.fold(empty(queries))(_.copy(queries = queries))

    case url =>
      Path.empty(Queries.fromURL(url))
  }
}
