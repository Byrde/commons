package org.byrde.http.server

trait ByrdeRoutes {
  def value: Seq[ByrdeRoute[_, _, _, _]]
}

object ByrdeRoutes {
  def apply(_route: ByrdeRoute[_, _, _, _]): ByrdeRoutes =
    new ByrdeRoutes {
      override def value: Seq[ByrdeRoute[_, _, _, _]] = Seq(_route)
    }
  
  def apply(_routes: Seq[ByrdeRoute[_, _, _, _]]): ByrdeRoutes =
    new ByrdeRoutes {
      override def value: Seq[ByrdeRoute[_, _, _, _]] = _routes
    }
}