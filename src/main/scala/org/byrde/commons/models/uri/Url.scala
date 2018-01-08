package org.byrde.commons.models.uri

case class Url(host: Host, path: Path) {
  def /(newPath: String): Path =
    path / newPath

  def +(newQuery: (String, String)): Queries =
    path + newQuery

  def ++(newQuery: Set[(String, String)]): Queries =
    path ++ newQuery

  override def toString: String =
    (host.toString + path.toString).trim
}
