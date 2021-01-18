package org.byrde.http.server

trait MaterializedRoutes {
  self =>
  
  type Routes = Seq[MaterializedRoute[_, _, _, _]]
  
  implicit def route2Routes(route: MaterializedRoute[_, _, _, _]): Routes =
    Seq(route)
  
  implicit class RichMaterializedRoute(value: MaterializedRoute[_, _, _, _]) {
    def ~ (route: MaterializedRoute[_, _, _, _]): Routes =
      value :+ route
    
    def ~ (routes: MaterializedRoutes): Routes =
      value ++ routes.routes
  }
  
  implicit class RichRoutes(value: Routes) {
    def ~ (route: MaterializedRoute[_, _, _, _]): Routes =
      value :+ route
    
    def ~ (routes: MaterializedRoutes): Routes =
      value ++ routes.routes
  }
  
  def routes: Routes
  
  def ~ (route: org.byrde.http.server.MaterializedRoute[_, _, _, _]): MaterializedRoutes =
    new MaterializedRoutes {
      override lazy val routes: Routes = self.routes :+ route
    }
  
  def ~ (_routes: Routes): MaterializedRoutes =
    new MaterializedRoutes {
      override lazy val routes: Routes = self.routes ++ _routes
    }
  
  def ~ (_routes: MaterializedRoutes): MaterializedRoutes =
    new MaterializedRoutes {
      override lazy val routes: Routes = self.routes ++ _routes.routes
    }
}