package org.byrde.tapir

trait TapirRoutes {
  def value: Seq[TapirRoute[_, _, _, _]]
}

object TapirRoutes {
  def apply(_route: TapirRoute[_, _, _, _]): TapirRoutes =
    new TapirRoutes {
      override def value: Seq[TapirRoute[_, _, _, _]] = Seq(_route)
    }
  
  def apply(_routes: Seq[TapirRoute[_, _, _, _]]): TapirRoutes =
    new TapirRoutes {
      override def value: Seq[TapirRoute[_, _, _, _]] = _routes
    }
}