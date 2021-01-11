package org.byrde.http.server

trait Routes {
  self =>
  
  type MaterializedRoutes = Seq[MaterializedRoute[_, _, _, _]]
  
  implicit def route2Routes(route: MaterializedRoute[_, _, _, _]): MaterializedRoutes =
    Seq(route)
  
  implicit class RichMaterializedRoute(value: MaterializedRoute[_, _, _, _]) {
    def ~ (route: MaterializedRoute[_, _, _, _]): MaterializedRoutes =
      value :+ route
    
    def ~ (routes: Routes): MaterializedRoutes =
      value ++ routes.routes
  }
  
  implicit class RichRoutes(value: MaterializedRoutes) {
    def ~ (route: MaterializedRoute[_, _, _, _]): MaterializedRoutes =
      value :+ route
    
    def ~ (routes: Routes): MaterializedRoutes =
      value ++ routes.routes
  }
  
  def routes: MaterializedRoutes
  
  def ~ (route: org.byrde.http.server.MaterializedRoute[_, _, _, _]): Routes =
    new Routes {
      override def routes: MaterializedRoutes = self.routes :+ route
    }
  
  def ~ (_routes: MaterializedRoutes): Routes =
    new Routes {
      override def routes: MaterializedRoutes = self.routes ++ _routes
    }
  
  def ~ (_routes: Routes): Routes =
    new Routes {
      override def routes: MaterializedRoutes = self.routes ++ _routes.routes
    }
}