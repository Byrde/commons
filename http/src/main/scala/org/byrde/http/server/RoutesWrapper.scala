package org.byrde.http.server

trait RoutesWrapper {
  self =>
  
  type Routes = Seq[RouteWrapper[_, _, _, _]]
  
  implicit def route2Routes(route: RouteWrapper[_, _, _, _]): Routes =
    Seq(route)
  
  implicit class RichRoute(value: RouteWrapper[_, _, _, _]) {
    def ~ (route: RouteWrapper[_, _, _, _]): Routes =
      value :+ route
    
    def ~ (routes: RoutesWrapper): Routes =
      value ++ routes.routes
  }
  
  implicit class RichRoutes(value: Routes) {
    def ~ (route: RouteWrapper[_, _, _, _]): Routes =
      value :+ route
    
    def ~ (routes: RoutesWrapper): Routes =
      value ++ routes.routes
  }
  
  def routes: Routes
  
  def ~ (route: org.byrde.http.server.RouteWrapper[_, _, _, _]): RoutesWrapper =
    new RoutesWrapper {
      override def routes: Routes = self.routes :+ route
    }
  
  def ~ (_routes: Routes): RoutesWrapper =
    new RoutesWrapper {
      override def routes: Routes = self.routes ++ _routes
    }
  
  def ~ (_routes: RoutesWrapper): RoutesWrapper =
    new RoutesWrapper {
      override def routes: Routes = self.routes ++ _routes.routes
    }
}