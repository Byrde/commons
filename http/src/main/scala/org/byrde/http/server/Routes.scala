package org.byrde.http.server

trait Routes {
  self =>
  
  def routes: Seq[Route[_, _, _, _]]
  
  def ~ (route: org.byrde.http.server.Route[_, _, _, _]): Routes =
    new Routes {
      override def routes: Seq[Route[_, _, _, _]] = self.routes :+ route
    }
  
  def ~ (_routes: Routes): Routes =
    new Routes {
      override def routes: Seq[Route[_, _, _, _]] = routes ++ _routes.routes
    }
}