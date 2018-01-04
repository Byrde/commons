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
