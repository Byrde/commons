package org.byrde.tapir

trait TapirRoutes {
  def value: Seq[TapirRoute]
}

object TapirRoutes {
  def apply(_route: TapirRoute): TapirRoutes =
    new TapirRoutes {
      override def value: Seq[TapirRoute] = Seq(_route)
    }
  
  def apply(_routes: Seq[TapirRoute]): TapirRoutes =
    new TapirRoutes {
      override def value: Seq[TapirRoute] = _routes
    }
}