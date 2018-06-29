package org.byrde.commons.models.uri

case class Path(path: String, queries: Queries = Queries(Set())) {
  def /(newPath: String): Path =
    this.copy(path = path + s"/$newPath")

  def +(newQuery: (String, String)): Queries =
    queries + newQuery

  def ++(newQuery: Set[(String, String)]): Queries =
    queries ++ newQuery

  override def toString: String =
    s"/$path" + queries.toString
}

object Path {
  val empty: Path =
    Path("")

  def fromString(value: String): Path = {
    val originalPath =
      value
        .split("/")
        .drop(1)

    val startOpt =
      originalPath
        .headOption
        .map(Path.apply(_))

    val path =
      startOpt
        .map { start =>
          originalPath
            .drop(1)
            .foldLeft(start)(_ / _)
        }

    path.getOrElse(empty)
  }
}