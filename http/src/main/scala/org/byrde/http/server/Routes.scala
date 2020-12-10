package org.byrde.http.server

trait Routes {
  def value: Seq[Route[_, _, _, _]]
}

object Routes {
  def apply(_route: Route[_, _, _, _]): Routes =
    new Routes {
      override def value: Seq[Route[_, _, _, _]] = Seq(_route)
    }
  
  def apply(_routes: Seq[Route[_, _, _, _]]): Routes =
    new Routes {
      override def value: Seq[Route[_, _, _, _]] = _routes
    }
}